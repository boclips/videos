package com.boclips.search.service.infrastructure

import com.boclips.search.service.domain.common.IndexWriter
import com.boclips.search.service.domain.common.ProgressNotifier
import mu.KLogging
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest
import org.elasticsearch.action.admin.indices.get.GetIndexRequest
import org.elasticsearch.action.bulk.BulkRequest
import org.elasticsearch.action.delete.DeleteRequest
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.support.IndicesOptions
import org.elasticsearch.action.support.WriteRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.unit.TimeValue
import org.elasticsearch.common.xcontent.XContentType

data class IndexParameters(val numberOfShards: Int)

abstract class AbstractIndexWriter<T>(
    private val indexConfiguration: IndexConfiguration,
    val client: RestHighLevelClient,
    private val indexParameters: IndexParameters,
    private val esIndex: Index
) : IndexWriter<T> {
    companion object : KLogging() {
        private const val UPSERT_BATCH_SIZE = 1000
    }

    override fun safeRebuildIndex(items: Sequence<T>, notifier: ProgressNotifier?) {
        val newIndexName = esIndex.generateIndexName()

        notifier?.send("Creating index using ${indexParameters.numberOfShards} shards...")
        createIndex(newIndexName)

        upsertToIndex(items, newIndexName, notifier)

        notifier?.send("Switching alias...")
        switchAliasToIndex(indexName = newIndexName, alias = esIndex.getIndexAlias())

        notifier?.send("Deleting previous aliases...")
        val allItemIndicesExceptNew = "${esIndex.getIndexWildcard()},-$newIndexName"
        deleteIndex(allItemIndicesExceptNew)

        notifier?.complete()
    }

    override fun removeFromSearch(itemId: String) {
        val request = DeleteRequest(
            esIndex.getIndexAlias(),
            esIndex.getESType(), itemId
        )
        request.refreshPolicy = WriteRequest.RefreshPolicy.WAIT_UNTIL
        client.delete(request, RequestOptions.DEFAULT)
    }

    override fun bulkRemoveFromSearch(items: List<String>) {
        items.windowed(size = UPSERT_BATCH_SIZE, step = UPSERT_BATCH_SIZE, partialWindows = true)
            .forEachIndexed { batchIndex, list ->
                logger.info { "[Batch $batchIndex] removing ${items.size} item(s)" }

                val request = list
                    .map {
                        DeleteRequest(
                            esIndex.getIndexAlias(),
                            esIndex.getESType(), it
                        )
                    }
                    .fold(BulkRequest()) { bulkRequest, indexRequest ->
                        bulkRequest.add(indexRequest)
                    }

                request.timeout(TimeValue.timeValueMinutes(2))
                request.refreshPolicy = WriteRequest.RefreshPolicy.WAIT_UNTIL

                val result = client.bulk(request, RequestOptions.DEFAULT)

                if (result.hasFailures()) {
                    throw Error("Batch removed failed: ${result.buildFailureMessage()}")
                }
                logger.info { "[Batch $batchIndex] Successfully removed ${result.items.size} item(s)" }
            }
    }

    override fun upsert(items: Sequence<T>, notifier: ProgressNotifier?) {
        upsertToIndex(items, esIndex.getIndexAlias())
    }

    override fun makeSureIndexIsThere() {
        if (!client.indices().exists(
                GetIndexRequest().indices(esIndex.getIndexAlias()),
                RequestOptions.DEFAULT
            )
        ) {
            logger.info { "Initialising index with empty sequence" }
            safeRebuildIndex(emptySequence())
        }
    }

    private fun upsertToIndex(items: Sequence<T>, indexName: String, notifier: ProgressNotifier? = null) {
        items.windowed(size = UPSERT_BATCH_SIZE, step = UPSERT_BATCH_SIZE, partialWindows = true)
            .forEachIndexed { idx, batch ->
                notifier?.send("Starting upsert batch $idx")
                this.upsertBatch(idx, batch, indexName)
            }
    }

    private fun createIndex(indexName: String) {
        val createIndexRequest = CreateIndexRequest(indexName)
            .settings(indexConfiguration.defaultEnglishSettings(indexParameters.numberOfShards))
            .mapping(esIndex.getESType(), indexConfiguration.generateMapping())

        logger.info("Creating index $indexName")
        client.indices().create(createIndexRequest, RequestOptions.DEFAULT)
    }

    private fun switchAliasToIndex(indexName: String, alias: String) {
        val request = IndicesAliasesRequest()
        val removeAliases = IndicesAliasesRequest.AliasActions(IndicesAliasesRequest.AliasActions.Type.REMOVE)
            .indices(esIndex.getIndexWildcard())
            .alias(alias)
        val addAliases = IndicesAliasesRequest.AliasActions(IndicesAliasesRequest.AliasActions.Type.ADD)
            .index(indexName)
            .alias(alias)

        request.addAliasAction(removeAliases)
        request.addAliasAction(addAliases)

        logger.info("Switching alias ($alias) to index ($indexName)")
        client.indices().updateAliases(request, RequestOptions.DEFAULT)
        logger.info("Switched alias ($alias) to index ($indexName)")
    }

    private fun deleteIndex(indexName: String) {
        if (indexExists(indexName)) {
            logger.info("Deleting index $indexName")
            val deleteRequest = DeleteIndexRequest(indexName).indicesOptions(IndicesOptions.LENIENT_EXPAND_OPEN)
            client.indices().delete(deleteRequest, RequestOptions.DEFAULT)
            logger.info("Index $indexName deleted")
        }
    }

    private fun upsertBatch(batchIndex: Int, items: List<T>, indexName: String) {
        logger.info { "[Batch $batchIndex] Indexing ${items.size} item(s)" }

        val request = items
            .map { indexRequest(it, indexName) }
            .fold(BulkRequest()) { bulkRequest, indexRequest ->
                bulkRequest.add(indexRequest)
            }

        request.timeout(TimeValue.timeValueMinutes(2))
        request.refreshPolicy = WriteRequest.RefreshPolicy.WAIT_UNTIL

        val result = client.bulk(request, RequestOptions.DEFAULT)

        if (result.hasFailures()) {
            throw Error("Batch indexing failed: ${result.buildFailureMessage()}")
        }
        logger.info { "[Batch $batchIndex] Successfully indexed ${result.items.size} item(s)" }
    }

    private fun indexRequest(item: T, indexName: String): IndexRequest {
        val document = ESObjectMapper.get().writeValueAsString(
            serializeToIndexDocument(item)
        )

        return IndexRequest(
            indexName,
            esIndex.getESType(), getIdentifier(item)
        )
            .source(document, XContentType.JSON)
    }

    abstract fun serializeToIndexDocument(entry: T): Any
    abstract fun getIdentifier(entry: T): String

    private fun indexExists(index: String) =
        client.indices().exists(GetIndexRequest().indices(index), RequestOptions.DEFAULT)
}
