package com.boclips.search.service.infrastructure

import com.boclips.search.service.domain.GenericSearchServiceAdmin
import com.boclips.search.service.domain.VideoMetadata
import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.impl.client.BasicCredentialsProvider
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest
import org.elasticsearch.action.admin.indices.get.GetIndexRequest
import org.elasticsearch.action.bulk.BulkRequest
import org.elasticsearch.action.delete.DeleteRequest
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.support.WriteRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.unit.TimeValue
import org.elasticsearch.common.xcontent.XContentType

class ElasticSearchServiceAdmin(val config: ElasticSearchConfig) : GenericSearchServiceAdmin<VideoMetadata> {
    private val client: RestHighLevelClient

    init {
        val credentialsProvider = BasicCredentialsProvider()
        credentialsProvider.setCredentials(AuthScope.ANY, UsernamePasswordCredentials(config.username, config.password))

        val builder = RestClient.builder(HttpHost(config.host, config.port, config.scheme)).setHttpClientConfigCallback { httpClientBuilder ->
            httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
        }
        client = RestHighLevelClient(builder)
    }

    override fun resetIndex() {
        deleteIndex()
        configureIndex()
    }

    override fun removeFromSearch(videoId: String) {
        val request = DeleteRequest(ElasticSearchService.ES_INDEX, ElasticSearchService.ES_TYPE, videoId)
        request.refreshPolicy = WriteRequest.RefreshPolicy.WAIT_UNTIL
        client.delete(request, RequestOptions.DEFAULT)
    }

    override fun upsert(videos: Sequence<VideoMetadata>) {
        val batchSize = 2000
        videos.windowed(size = batchSize, step = batchSize, partialWindows = true).forEachIndexed(this::upsertBatch)
    }

    private fun configureIndex() {
        val indexConfiguration = IndexConfiguration()
        val createIndexRequest = CreateIndexRequest(ElasticSearchService.ES_INDEX)
                .settings(indexConfiguration.generateIndexSettings())
                .mapping("asset", indexConfiguration.generateVideoMapping())

        ElasticSearchService.logger.info("Creating index ${ElasticSearchService.ES_INDEX}")
        client.indices().create(createIndexRequest, RequestOptions.DEFAULT)
    }

    private fun deleteIndex() {
        if (indexExists(ElasticSearchService.ES_INDEX)) {
            ElasticSearchService.logger.info("Deleting index ${ElasticSearchService.ES_INDEX}")
            client.indices().delete(DeleteIndexRequest(ElasticSearchService.ES_INDEX), RequestOptions.DEFAULT)
            ElasticSearchService.logger.info("Index ${ElasticSearchService.ES_INDEX} deleted")
        }
    }

    private fun upsertBatch(batchIndex: Int, videos: List<VideoMetadata>) {
        ElasticSearchService.logger.info { "[Batch $batchIndex] Indexing ${videos.size} asset(s)" }

        val request = videos
                .map(this::indexRequest)
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

    private fun indexRequest(video: VideoMetadata): IndexRequest {
        val document = ElasticObjectMapper.get().writeValueAsString(ElasticSearchVideo(
                id = video.id,
                title = video.title,
                description = video.description,
                contentProvider = video.contentProvider,
                keywords = video.keywords,
                tags = video.tags
        ))

        return IndexRequest(ElasticSearchService.ES_INDEX, ElasticSearchService.ES_TYPE, video.id)
                .source(document, XContentType.JSON)
    }

    private fun indexExists(index: String) = client.indices().exists(GetIndexRequest().indices(index), RequestOptions.DEFAULT)
}