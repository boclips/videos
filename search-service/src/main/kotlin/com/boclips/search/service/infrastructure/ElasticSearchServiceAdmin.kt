package com.boclips.search.service.infrastructure

import com.boclips.search.service.domain.GenericSearchServiceAdmin
import com.boclips.search.service.domain.ProgressNotifier
import com.boclips.search.service.domain.VideoMetadata
import mu.KLogging
import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.impl.client.BasicCredentialsProvider
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
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.unit.TimeValue
import org.elasticsearch.common.xcontent.XContentType

class ElasticSearchServiceAdmin(val config: ElasticSearchConfig) : GenericSearchServiceAdmin<VideoMetadata> {
    private val client: RestHighLevelClient

    companion object : KLogging() {
        private const val ES_TYPE = "asset"
        private const val UPSERT_BATCH_SIZE = 2000
    }

    init {
        val credentialsProvider = BasicCredentialsProvider()
        credentialsProvider.setCredentials(AuthScope.ANY, UsernamePasswordCredentials(config.username, config.password))

        val builder = RestClient.builder(HttpHost(config.host, config.port, config.scheme))
                .setHttpClientConfigCallback { httpClientBuilder ->
                    httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
                }
        client = RestHighLevelClient(builder)
    }

    override fun safeRebuildIndex(videos: Sequence<VideoMetadata>, notifier: ProgressNotifier?) {
        val newIndexName = ElasticSearchIndex.generateIndexName()

        notifier?.send("Creating index...")
        createIndex(newIndexName)

        upsertToIndex(videos, newIndexName, notifier)

        notifier?.send("Switching alias...")
        switchAliasToIndex(indexName = newIndexName, alias = ElasticSearchIndex.ES_INDEX_ALIAS)

        notifier?.send("Deleting previous aliases...")
        val allVideoIndicesExceptNew = "${ElasticSearchIndex.ES_INDEX_WILDCARD},-$newIndexName"
        deleteIndex(allVideoIndicesExceptNew)

        notifier?.complete()
    }

    override fun removeFromSearch(videoId: String) {
        val request = DeleteRequest(ElasticSearchIndex.ES_INDEX_ALIAS, ES_TYPE, videoId)
        request.refreshPolicy = WriteRequest.RefreshPolicy.WAIT_UNTIL
        client.delete(request, RequestOptions.DEFAULT)
    }

    override fun upsert(videos: Sequence<VideoMetadata>, notifier: ProgressNotifier?) {
        makeSureIndexIsThere()
        upsertToIndex(videos, ElasticSearchIndex.ES_INDEX_ALIAS)
    }

    @Synchronized
    private fun makeSureIndexIsThere() {
        if (!client.indices().exists(GetIndexRequest().indices(ElasticSearchIndex.ES_INDEX_ALIAS), RequestOptions.DEFAULT)) {
            safeRebuildIndex(emptySequence())
        }
    }

    private fun upsertToIndex(videos: Sequence<VideoMetadata>, indexName: String, notifier: ProgressNotifier? = null) {
        videos.windowed(size = UPSERT_BATCH_SIZE, step = UPSERT_BATCH_SIZE, partialWindows = true)
                .forEachIndexed { idx, batch ->
                    notifier?.send("Starting upsert batch $idx")
                    this.upsertBatch(idx, batch, indexName)
                }
    }

    private fun createIndex(indexName: String) {
        val indexConfiguration = IndexConfiguration()
        val createIndexRequest = CreateIndexRequest(indexName)
                .settings(indexConfiguration.generateIndexSettings())
                .mapping("asset", indexConfiguration.generateVideoMapping())

        logger.info("Creating index $indexName")
        client.indices().create(createIndexRequest, RequestOptions.DEFAULT)
    }

    private fun switchAliasToIndex(indexName: String, alias: String) {
        val request = IndicesAliasesRequest()
        val removeAliases = IndicesAliasesRequest.AliasActions(IndicesAliasesRequest.AliasActions.Type.REMOVE)
                .indices(ElasticSearchIndex.ES_INDEX_WILDCARD)
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

    private fun upsertBatch(batchIndex: Int, videos: List<VideoMetadata>, indexName: String) {
        logger.info { "[Batch $batchIndex] Indexing ${videos.size} asset(s)" }

        val request = videos
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
        logger.info { "[Batch $batchIndex] Successfully indexed ${result.items.size} asset(s)" }
    }

    private fun indexRequest(video: VideoMetadata, indexName: String): IndexRequest {
        val document = ElasticObjectMapper.get().writeValueAsString(
                ElasticSearchVideo(
                        id = video.id,
                        title = video.title,
                        description = video.description,
                        contentProvider = video.contentProvider,
                        keywords = video.keywords,
                        tags = video.tags
                )
        )

        return IndexRequest(indexName, ES_TYPE, video.id)
                .source(document, XContentType.JSON)
    }

    private fun indexExists(index: String) =
            client.indices().exists(GetIndexRequest().indices(index), RequestOptions.DEFAULT)
}