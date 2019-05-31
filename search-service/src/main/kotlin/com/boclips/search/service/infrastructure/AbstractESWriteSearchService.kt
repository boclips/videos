//package com.boclips.search.service.infrastructure
//
//import com.boclips.search.service.domain.ProgressNotifier
//import com.boclips.search.service.domain.WriteSearchService
//import com.boclips.search.service.domain.videos.model.VideoMetadata
//import com.boclips.search.service.infrastructure.videos.ESVideo
//import com.boclips.search.service.infrastructure.videos.ESVideosIndex
//import mu.KLogging
//import org.apache.http.HttpHost
//import org.apache.http.auth.AuthScope
//import org.apache.http.auth.UsernamePasswordCredentials
//import org.apache.http.impl.client.BasicCredentialsProvider
//import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest
//import org.elasticsearch.action.admin.indices.create.CreateIndexRequest
//import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest
//import org.elasticsearch.action.admin.indices.get.GetIndexRequest
//import org.elasticsearch.action.bulk.BulkRequest
//import org.elasticsearch.action.delete.DeleteRequest
//import org.elasticsearch.action.index.IndexRequest
//import org.elasticsearch.action.support.IndicesOptions
//import org.elasticsearch.action.support.WriteRequest
//import org.elasticsearch.client.RequestOptions
//import org.elasticsearch.client.RestClient
//import org.elasticsearch.client.RestHighLevelClient
//import org.elasticsearch.common.unit.TimeValue
//import org.elasticsearch.common.xcontent.XContentType
//
//abstract class AbstractESWriteSearchService<T>(val config: ESConfig) : WriteSearchService<T> {
//    private val client: RestHighLevelClient
//
//    companion object : KLogging() {
//        private const val ES_TYPE = "video"
//        private const val UPSERT_BATCH_SIZE = 2000
//    }
//
//    init {
//        val credentialsProvider = BasicCredentialsProvider()
//        credentialsProvider.setCredentials(AuthScope.ANY, UsernamePasswordCredentials(config.username, config.password))
//
//        val builder = RestClient.builder(HttpHost(config.host, config.port, config.scheme))
//            .setHttpClientConfigCallback { httpClientBuilder ->
//                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
//            }
//        client = RestHighLevelClient(builder)
//    }
//
//    override fun safeRebuildIndex(videos: Sequence<T>, notifier: ProgressNotifier?) {
//        val newIndexName = ESVideosIndex.generateIndexName()
//
//        notifier?.send("Creating index...")
//        createIndex(newIndexName)
//
//        upsertToIndex(videos, newIndexName, notifier)
//
//        notifier?.send("Switching alias...")
//        switchAliasToIndex(indexName = newIndexName, alias = ESVideosIndex.getIndexAlias())
//
//        notifier?.send("Deleting previous aliases...")
//        val allVideoIndicesExceptNew = "${ESVideosIndex.getIndexWildcard()},-$newIndexName"
//        deleteIndex(allVideoIndicesExceptNew)
//
//        notifier?.complete()
//    }
//
//    override fun removeFromSearch(videoId: String) {
//        val request = DeleteRequest(
//            ESVideosIndex.getIndexAlias(),
//            ES_TYPE, videoId)
//        request.refreshPolicy = WriteRequest.RefreshPolicy.WAIT_UNTIL
//        client.delete(request, RequestOptions.DEFAULT)
//    }
//
//    override fun upsert(videos: Sequence<VideoMetadata>, notifier: ProgressNotifier?) {
//        makeSureIndexIsThere()
//        upsertToIndex(videos, ESVideosIndex.getIndexAlias())
//    }
//
//    @Synchronized
//    private fun makeSureIndexIsThere() {
//        if (!client.indices().exists(
//                GetIndexRequest().indices(ESVideosIndex.getIndexAlias()),
//                RequestOptions.DEFAULT
//            )
//        ) {
//            safeRebuildIndex(emptySequence())
//        }
//    }
//
//    private fun upsertToIndex(videos: Sequence<VideoMetadata>, indexName: String, notifier: ProgressNotifier? = null) {
//        videos.windowed(size = UPSERT_BATCH_SIZE, step = UPSERT_BATCH_SIZE, partialWindows = true)
//            .forEachIndexed { idx, batch ->
//                notifier?.send("Starting upsert batch $idx")
//                this.upsertBatch(idx, batch, indexName)
//            }
//    }
//
//    private fun createIndex(indexName: String) {
//        val indexConfiguration = IndexConfiguration()
//        val createIndexRequest = CreateIndexRequest(indexName)
//            .settings(indexConfiguration.defaultEnglishSettings())
//            .mapping("video", indexConfiguration.generateVideoMapping())
//
//        logger.info("Creating index $indexName")
//        client.indices().create(createIndexRequest, RequestOptions.DEFAULT)
//    }
//
//    private fun switchAliasToIndex(indexName: String, alias: String) {
//        val request = IndicesAliasesRequest()
//        val removeAliases = IndicesAliasesRequest.AliasActions(IndicesAliasesRequest.AliasActions.Type.REMOVE)
//            .indices(ESVideosIndex.getIndexWildcard())
//            .alias(alias)
//        val addAliases = IndicesAliasesRequest.AliasActions(IndicesAliasesRequest.AliasActions.Type.ADD)
//            .index(indexName)
//            .alias(alias)
//
//        request.addAliasAction(removeAliases)
//        request.addAliasAction(addAliases)
//
//        logger.info("Switching alias ($alias) to index ($indexName)")
//        client.indices().updateAliases(request, RequestOptions.DEFAULT)
//        logger.info("Switched alias ($alias) to index ($indexName)")
//    }
//
//    private fun deleteIndex(indexName: String) {
//        if (indexExists(indexName)) {
//            logger.info("Deleting index $indexName")
//            val deleteRequest = DeleteIndexRequest(indexName).indicesOptions(IndicesOptions.LENIENT_EXPAND_OPEN)
//            client.indices().delete(deleteRequest, RequestOptions.DEFAULT)
//            logger.info("Index $indexName deleted")
//        }
//    }
//
//    private fun upsertBatch(batchIndex: Int, videos: List<VideoMetadata>, indexName: String) {
//        logger.info { "[Batch $batchIndex] Indexing ${videos.size} video(s)" }
//
//        val request = videos
//            .map { indexRequest(it, indexName) }
//            .fold(BulkRequest()) { bulkRequest, indexRequest ->
//                bulkRequest.add(indexRequest)
//            }
//
//        request.timeout(TimeValue.timeValueMinutes(2))
//        request.refreshPolicy = WriteRequest.RefreshPolicy.WAIT_UNTIL
//
//        val result = client.bulk(request, RequestOptions.DEFAULT)
//
//        if (result.hasFailures()) {
//            throw Error("Batch indexing failed: ${result.buildFailureMessage()}")
//        }
//        logger.info { "[Batch $batchIndex] Successfully indexed ${result.items.size} video(s)" }
//    }
//
//    private fun indexRequest(video: VideoMetadata, indexName: String): IndexRequest {
//        val document = ESObjectMapper.get().writeValueAsString(
//            ESVideo(
//                id = video.id,
//                title = video.title,
//                description = video.description,
//                contentProvider = video.contentProvider,
//                releaseDate = video.releaseDate,
//                keywords = video.keywords,
//                tags = video.tags,
//                durationSeconds = video.durationSeconds,
//                source = video.source.name,
//                transcript = video.transcript
//            )
//        )
//
//        return IndexRequest(indexName,
//            ES_TYPE, video.id)
//            .source(document, XContentType.JSON)
//    }
//
//    private fun indexExists(index: String) =
//        client.indices().exists(GetIndexRequest().indices(index), RequestOptions.DEFAULT)
//}
