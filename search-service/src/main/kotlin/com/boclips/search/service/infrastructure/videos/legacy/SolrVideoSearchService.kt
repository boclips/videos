package com.boclips.search.service.infrastructure.videos.legacy

import com.boclips.search.service.domain.common.ResultCounts
import com.boclips.search.service.domain.common.ProgressNotifier
import com.boclips.search.service.domain.common.SearchResults
import com.boclips.search.service.domain.common.model.IndexSearchRequest
import com.boclips.search.service.domain.common.model.PaginatedIndexSearchRequest
import com.boclips.search.service.domain.videos.legacy.LegacyVideoMetadata
import com.boclips.search.service.domain.videos.legacy.LegacyVideoSearchService
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.infrastructure.common.exceptions.CursorBasedRequestNotSupportedException
import mu.KLogging
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.impl.HttpSolrClient

class SolrVideoSearchService(host: String, port: Int) : LegacyVideoSearchService {
    companion object : KLogging() {
        private const val UPSERT_BATCH_SIZE = 4000
    }

    override fun safeRebuildIndex(items: Sequence<LegacyVideoMetadata>, notifier: ProgressNotifier?) {
        throw java.lang.UnsupportedOperationException("Not supported by SOLR search service")
    }

    val client = HttpSolrClient.Builder("http://$host:$port/solr/km").build()!!

    override fun upsert(items: Sequence<LegacyVideoMetadata>, notifier: ProgressNotifier?) {
        items.windowed(size = UPSERT_BATCH_SIZE, step = UPSERT_BATCH_SIZE, partialWindows = true)
            .forEachIndexed { batchIndex, videoBatch ->
                notifier?.send("Starting batch $batchIndex")
                this.upsertBatch(batchIndex, videoBatch)
            }
        notifier?.complete()
    }

    private fun upsertBatch(batchIndex: Int, videos: List<LegacyVideoMetadata>) {
        logger.info { "[Batch $batchIndex] Indexing ${videos.size} video(s) in Solr" }
        videos.map(SolrDocumentConverter::convert).forEach { client.add(it) }
        client.commit()
        logger.info { "[Batch $batchIndex] Successfully indexed ${videos.size} video(s) in Solr" }
    }

    override fun search(searchRequest: IndexSearchRequest<VideoQuery>): SearchResults {
        searchRequest as? PaginatedIndexSearchRequest
            ?: throw CursorBasedRequestNotSupportedException

        if (searchRequest.query.phrase.isNotEmpty()) {
            throw java.lang.UnsupportedOperationException()
        }
        val query = searchRequest.query.userQuery.ids.joinToString(separator = " OR ", prefix = "id:")
        val videoIds = client.query(SolrQuery(query)).results.toList()
        val elements = videoIds.map { it.getFieldValue("id").toString() }
        return SearchResults(elements = elements, counts = ResultCounts(totalHits = videoIds.size.toLong()))
    }

    override fun removeFromSearch(itemId: String) {
        fun <T> runAndThrow(f: () -> T): T =
            try {
                f()
            } catch (ex: Exception) {
                throw SolrException(ex)
            }

        runAndThrow {
            client.deleteById(itemId)
            client.commit()
        }
        logger.info { "Video $itemId removed from Solr" }
    }

    override fun bulkRemoveFromSearch(itemIds: List<String>) {
        itemIds.windowed(size = UPSERT_BATCH_SIZE, step = UPSERT_BATCH_SIZE, partialWindows = true)
            .forEachIndexed { batchIndex, videoBatch ->
                logger.info { "[Batch $batchIndex] Removing ${videoBatch.size} video(s) in Solr" }

                videoBatch.forEach { client.deleteById(it) }
                client.commit()

                logger.info { "[Batch $batchIndex] Successfully removed ${videoBatch.size} video(s) in Solr" }
            }
    }

    override fun makeSureIndexIsThere() {
    }
}
