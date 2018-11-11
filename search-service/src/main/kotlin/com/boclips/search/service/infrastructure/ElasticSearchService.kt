package com.boclips.search.service.infrastructure

import com.boclips.search.service.domain.SearchService
import com.boclips.search.service.domain.VideoMetadata
import com.boclips.search.service.infrastructure.IndexConfiguration.Companion.FIELD_DESCRIPTOR_SHINGLES
import mu.KLogging
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
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.support.WriteRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.unit.Fuzziness
import org.elasticsearch.common.unit.TimeValue
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.index.query.MultiMatchQueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.elasticsearch.search.rescore.QueryRescoreMode
import org.elasticsearch.search.rescore.QueryRescorerBuilder

class ElasticSearchService(val config: ElasticSearchConfig) : SearchService {
    companion object : KLogging() {
        const val ES_TYPE = "asset"
        const val ES_INDEX = "videos"
    }

    private val elasticSearchResultConverter = ElasticSearchResultConverter()
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

    override fun search(query: String): List<String> {
        return searchElasticSearch(query).map { it.id }
    }

    override fun removeFromSearch(videoId: String) {
        val request = DeleteRequest(ES_INDEX, ES_TYPE, videoId)
        request.refreshPolicy = WriteRequest.RefreshPolicy.WAIT_UNTIL
        client.delete(request, RequestOptions.DEFAULT)
    }

    private fun searchElasticSearch(query: String): List<ElasticSearchVideo> {
        val findMatchesQuery = QueryBuilders.multiMatchQuery(query, "title", "title.std", "description", "description.std", "contentProvider", "keywords")
                .type(MultiMatchQueryBuilder.Type.MOST_FIELDS)
                .minimumShouldMatch("75%")
                .fuzziness(Fuzziness.AUTO)

        val rescoreQuery = QueryBuilders.multiMatchQuery(query, "title.$FIELD_DESCRIPTOR_SHINGLES", "description.$FIELD_DESCRIPTOR_SHINGLES")
                .type(MultiMatchQueryBuilder.Type.MOST_FIELDS)

        val rescorer = QueryRescorerBuilder(rescoreQuery)
                .windowSize(100)
                .setScoreMode(QueryRescoreMode.Total)

        val searchRequest = SearchRequest(arrayOf(ES_INDEX),
                SearchSourceBuilder().query(findMatchesQuery).addRescorer(rescorer)
        )

        return client.search(searchRequest, RequestOptions.DEFAULT).hits.hits.map(elasticSearchResultConverter::convert)
    }

    override fun upsert(videos: Sequence<VideoMetadata>) {
        val batchSize = 2000
        videos.windowed(size = batchSize, step = batchSize, partialWindows = true).forEachIndexed(this::upsertBatch)
    }

    private fun upsertBatch(batchIndex: Int, videos: List<VideoMetadata>) {
        logger.info { "[Batch $batchIndex] Indexing ${videos.size} asset(s)" }

        val request = videos
                .map(this::indexRequest)
                .fold(BulkRequest()) { bulkRequest, indexRequest ->
                    bulkRequest.add(indexRequest)
                }

        request.timeout(TimeValue.timeValueMinutes(2))
        request.refreshPolicy = WriteRequest.RefreshPolicy.WAIT_UNTIL

        val result = client.bulk(request, RequestOptions.DEFAULT)

        if(result.hasFailures()) {
            throw Error("Batch indexing failed: ${result.buildFailureMessage()}")
        }
        logger.info { "[Batch $batchIndex] Successfully indexed ${result.items.size} asset(s)" }
    }

    private fun indexRequest(video: VideoMetadata): IndexRequest {
        val document = ElasticObjectMapper.get().writeValueAsString(ElasticSearchVideo(
                id = video.id,
                title = video.title,
                description = video.description,
                contentProvider = video.contentProvider,
                keywords = video.keywords
        ))

        return IndexRequest(ES_INDEX, ES_TYPE, video.id)
                .source(document, XContentType.JSON)
    }

    private fun configureIndex() {
        val indexConfiguration = IndexConfiguration()
        val createIndexRequest = CreateIndexRequest(ES_INDEX)
                .settings(indexConfiguration.generateIndexSettings())
                .mapping("asset", indexConfiguration.generateVideoMapping())

        logger.info("Creating index $ES_INDEX")
        client.indices().create(createIndexRequest, RequestOptions.DEFAULT)
    }

    private fun deleteIndex() {
        if (indexExists(ES_INDEX)) {
            logger.info("Deleting index $ES_INDEX")
            client.indices().delete(DeleteIndexRequest(ES_INDEX), RequestOptions.DEFAULT)
            logger.info("Index $ES_INDEX deleted")
        }
    }

    private fun indexExists(index: String) = client.indices().exists(GetIndexRequest().indices(index), RequestOptions.DEFAULT)
}
