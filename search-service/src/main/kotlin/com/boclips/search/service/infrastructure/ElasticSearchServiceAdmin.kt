package com.boclips.search.service.infrastructure

import com.boclips.search.service.domain.GenericSearchServiceAdmin
import com.boclips.search.service.domain.VideoMetadata
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

    companion object {
        private const val ES_TYPE = "asset"
        private const val UPSERT_BATCH_SIZE = 2000
    }

    init {
        val credentialsProvider = BasicCredentialsProvider()
        credentialsProvider.setCredentials(AuthScope.ANY, UsernamePasswordCredentials(config.username, config.password))

        val builder = RestClient.builder(HttpHost(config.host, config.port, config.scheme)).setHttpClientConfigCallback { httpClientBuilder ->
            httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
        }
        client = RestHighLevelClient(builder)
    }

    override fun safeRebuildIndex(videos: Sequence<VideoMetadata>) {
        val newIndexName = ElasticSearchIndex.generateIndexName()

        createIndex(newIndexName)
        upsertToIndex(videos, newIndexName)
        switchAliasToIndex(indexName = newIndexName, alias = ElasticSearchIndex.ES_INDEX_ALIAS)

        val allVideoIndicesExceptNew = "${ElasticSearchIndex.ES_INDEX_WILDCARD},-$newIndexName"
        deleteIndex(allVideoIndicesExceptNew)
        deleteIndex(ElasticSearchIndex.ES_LEGACY_INDEX) // TODO: Remove once every env has been rebuilt once
    }

    override fun removeFromSearch(videoId: String) {
        val request = DeleteRequest(ElasticSearchIndex.ES_INDEX_ALIAS, ES_TYPE, videoId)
        request.refreshPolicy = WriteRequest.RefreshPolicy.WAIT_UNTIL
        client.delete(request, RequestOptions.DEFAULT)
    }

    override fun upsert(videos: Sequence<VideoMetadata>) {
        upsertToIndex(videos, ElasticSearchIndex.ES_INDEX_ALIAS)
    }

    private fun upsertToIndex(videos: Sequence<VideoMetadata>, indexName: String) {
        videos.windowed(size = UPSERT_BATCH_SIZE, step = UPSERT_BATCH_SIZE, partialWindows = true).forEachIndexed { idx, batch ->
            this.upsertBatch(idx, batch, indexName)
        }
    }

    private fun createIndex(indexName: String) {
        val indexConfiguration = IndexConfiguration()
        val createIndexRequest = CreateIndexRequest(indexName)
                .settings(indexConfiguration.generateIndexSettings())
                .mapping("asset", indexConfiguration.generateVideoMapping())

        ElasticSearchService.logger.info("Creating index $indexName")
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

        ElasticSearchService.logger.info("Switching alias ($alias) to index ($indexName)")
        client.indices().updateAliases(request, RequestOptions.DEFAULT)
        ElasticSearchService.logger.info("Switched alias ($alias) to index ($indexName)")
    }

    private fun deleteIndex(indexName: String) {
        if (indexExists(indexName)) {
            ElasticSearchService.logger.info("Deleting index $indexName")
            val deleteRequest = DeleteIndexRequest(indexName).indicesOptions(IndicesOptions.LENIENT_EXPAND_OPEN)
            client.indices().delete(deleteRequest, RequestOptions.DEFAULT)
            ElasticSearchService.logger.info("Index $indexName deleted")
        }
    }

    private fun upsertBatch(batchIndex: Int, videos: List<VideoMetadata>, indexName: String) {
        ElasticSearchService.logger.info { "[Batch $batchIndex] Indexing ${videos.size} asset(s)" }

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
        ElasticSearchService.logger.info { "[Batch $batchIndex] Successfully indexed ${result.items.size} asset(s)" }
    }

    private fun indexRequest(video: VideoMetadata, indexName: String): IndexRequest {
        val document = ElasticObjectMapper.get().writeValueAsString(ElasticSearchVideo(
                id = video.id,
                title = video.title,
                description = video.description,
                contentProvider = video.contentProvider,
                keywords = video.keywords,
                tags = video.tags
        ))

        return IndexRequest(indexName, ES_TYPE, video.id)
                .source(document, XContentType.JSON)
    }

    private fun indexExists(index: String) = client.indices().exists(GetIndexRequest().indices(index), RequestOptions.DEFAULT)
}