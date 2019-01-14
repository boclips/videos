package com.boclips.search.service.infrastructure.legacy

import com.boclips.search.service.domain.PaginatedSearchRequest
import com.boclips.search.service.domain.Query
import com.boclips.search.service.domain.legacy.LegacySearchService
import com.boclips.search.service.domain.legacy.LegacyVideoMetadata
import com.boclips.search.service.domain.legacy.SolrDocumentNotFound
import com.boclips.search.service.domain.legacy.SolrException
import mu.KLogging
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.client.solrj.impl.HttpSolrClient

class SolrSearchService(host: String, port: Int) : LegacySearchService {
    companion object : KLogging() {
        private const val UPSERT_BATCH_SIZE = 2000
    }

    override fun safeRebuildIndex(videos: Sequence<LegacyVideoMetadata>) {
        throw java.lang.UnsupportedOperationException("Not supported by SOLR search service")
    }

    val client = HttpSolrClient("http://$host:$port/solr/km")

    override fun upsert(videos: Sequence<LegacyVideoMetadata>) {
        videos.windowed(size = UPSERT_BATCH_SIZE, step = UPSERT_BATCH_SIZE, partialWindows = true)
                .forEachIndexed(this::upsertBatch)
    }

    private fun upsertBatch(batchIndex: Int, videos: List<LegacyVideoMetadata>) {
        logger.info { "[Batch $batchIndex] Indexing ${videos.size} asset(s) in Solr" }
        videos.map(LegacyVideoMetadataToSolrInputDocumentConverter::convert).forEach { client.add(it) }
        client.commit()
        logger.info { "[Batch $batchIndex] Successfully indexed ${videos.size} asset(s) in Solr" }
    }

    override fun search(searchRequest: PaginatedSearchRequest): List<String> {
        if (searchRequest.query.phrase != null) {
            throw java.lang.UnsupportedOperationException()
        }
        val query = searchRequest.query.ids.joinToString(separator = " OR ", prefix = "id:")
        return client.query(SolrQuery(query)).results.toList().map { it.getFieldValue("id").toString() }
    }

    override fun count(query: Query): Long {
        throw java.lang.UnsupportedOperationException("Not supported by SOLR search service")
    }

    override fun removeFromSearch(videoId: String) {
        fun <T> runAndThrow(f: () -> T): T =
                try {
                    f()
                } catch (ex: Exception) {
                    throw SolrException(ex)
                }

        runAndThrow { client.getById(videoId) } ?: throw SolrDocumentNotFound(videoId)
        runAndThrow {
            client.deleteById(videoId)
            client.commit()
        }
        logger.info { "Video $videoId removed from Solr" }
    }
}