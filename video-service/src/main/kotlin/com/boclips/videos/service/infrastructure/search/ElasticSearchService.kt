package com.boclips.videos.service.infrastructure.search

import com.boclips.kalturaclient.KalturaClient
import com.boclips.kalturaclient.media.MediaEntry
import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.service.SearchService
import com.boclips.videos.service.infrastructure.event.EventService
import com.boclips.videos.service.infrastructure.event.RequestId
import com.boclips.videos.service.infrastructure.search.VideoInformationAggregator.convert
import mu.KLogging
import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.impl.client.BasicCredentialsProvider
import org.elasticsearch.action.get.GetRequest
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.unit.Fuzziness
import org.elasticsearch.index.query.MultiMatchQueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.elasticsearch.search.rescore.QueryRescoreMode
import org.elasticsearch.search.rescore.QueryRescorerBuilder
import java.time.ZonedDateTime
import java.util.*

class ElasticSearchService(
        private val elasticSearchResultConverter: ElasticSearchResultConverter,
        private val kalturaClient: KalturaClient,
        private val eventService: EventService,
        private val requestId: RequestId,
        elasticSearchProperties: ElasticSearchProperties
) : SearchService {
    private val client: RestHighLevelClient

    companion object : KLogging() {
        fun extractKalturaReferenceIds(videos: List<ElasticSearchVideo>) =
                videos.map { it.referenceId }

    }

    init {
        val credentialsProvider = BasicCredentialsProvider()
        credentialsProvider.setCredentials(AuthScope.ANY, UsernamePasswordCredentials(elasticSearchProperties.username, elasticSearchProperties.password))

        val builder = RestClient.builder(HttpHost(elasticSearchProperties.host, elasticSearchProperties.port, elasticSearchProperties.scheme)).setHttpClientConfigCallback { httpClientBuilder ->
            httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
        }
        client = RestHighLevelClient(builder)
    }

    override fun findById(id: String): Video? {
        val elasticSearchVideo: ElasticSearchVideo = client
                .get(GetRequest("videos", "_all", id))
                .let(elasticSearchResultConverter::convert) ?: return null

        val kalturaVideo = kalturaClient.getMediaEntriesByReferenceId(elasticSearchVideo.referenceId).firstOrNull()
                ?: return null

        return convert(elasticSearchVideo, kalturaVideo)
    }

    override fun search(query: String): List<Video> {
        val elasticSearchVideos = searchElasticSearch(query)

        val referenceIds = extractKalturaReferenceIds(elasticSearchVideos)
        logger.info("Retrieving media entries for reference ids: ${referenceIds.joinToString(",")}")
        val mediaEntries: Map<String, MediaEntry> = getKalturaVideoByReferenceId(referenceIds)

        requestId.id = UUID.randomUUID().toString()

        eventService.saveEvent(SearchEvent(ZonedDateTime.now(), requestId.id!!, query, elasticSearchVideos.size))

        return convert(elasticSearchVideos, mediaEntries)
    }

    private fun searchElasticSearch(query: String): List<ElasticSearchVideo> {
        val findMatchesQuery = QueryBuilders.multiMatchQuery(query, "title", "title.std", "description", "description.std", "keywords")
                .type(MultiMatchQueryBuilder.Type.MOST_FIELDS)
                .minimumShouldMatch("75%")
                .fuzziness(Fuzziness.AUTO)

        val rescoreQuery = QueryBuilders.multiMatchQuery(query, "title.shingles", "description.shingles")
                .type(MultiMatchQueryBuilder.Type.MOST_FIELDS)

        val rescorer = QueryRescorerBuilder(rescoreQuery)
                .windowSize(100)
                .setScoreMode(QueryRescoreMode.Total)

        val searchRequest = SearchRequest(arrayOf("videos"),
                SearchSourceBuilder().query(findMatchesQuery).addRescorer(rescorer)
        )

        return client.search(searchRequest).hits.hits.map(elasticSearchResultConverter::convert)
    }

    private fun getKalturaVideoByReferenceId(referenceIds: List<String>): Map<String, MediaEntry> {
        val mediaEntries: Map<String, List<MediaEntry>> = kalturaClient.getMediaEntriesByReferenceIds(referenceIds)
        return mediaEntries.mapValues { it.value.first() }
    }


}
