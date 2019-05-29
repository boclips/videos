package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.PaginatedSearchRequest
import com.boclips.search.service.domain.videos.model.SourceType
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.domain.videos.VideoSearchService
import com.boclips.search.service.infrastructure.ElasticSearchConfig
import com.boclips.search.service.infrastructure.ElasticSearchIndex
import com.boclips.search.service.infrastructure.IndexConfiguration.Companion.FIELD_DESCRIPTOR_SHINGLES
import mu.KLogging
import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.impl.client.BasicCredentialsProvider
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.unit.Fuzziness
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.IdsQueryBuilder
import org.elasticsearch.index.query.MatchPhraseQueryBuilder
import org.elasticsearch.index.query.MultiMatchQueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.index.query.RangeQueryBuilder
import org.elasticsearch.index.query.TermQueryBuilder
import org.elasticsearch.search.SearchHits
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.elasticsearch.search.rescore.QueryRescoreMode
import org.elasticsearch.search.rescore.QueryRescorerBuilder
import org.elasticsearch.search.sort.SortOrder
import java.time.Duration
import java.time.LocalDate

class ElasticVideoSearchService(val config: ElasticSearchConfig) :
    VideoSearchService {
    companion object : KLogging();

    private val elasticSearchResultConverter =
        ElasticSearchVideoConverter()

    private val client: RestHighLevelClient

    init {
        val credentialsProvider = BasicCredentialsProvider()
        credentialsProvider.setCredentials(AuthScope.ANY, UsernamePasswordCredentials(config.username, config.password))

        val builder = RestClient.builder(HttpHost(config.host, config.port, config.scheme))
            .setHttpClientConfigCallback { httpClientBuilder ->
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
            }
        client = RestHighLevelClient(builder)
    }

    override fun search(searchRequest: PaginatedSearchRequest<VideoQuery>): List<String> {
        return searchElasticSearch(searchRequest.query, searchRequest.startIndex, searchRequest.windowSize)
            .map(elasticSearchResultConverter::convert)
            .map { it.id }
    }

    override fun count(videoQuery: VideoQuery): Long {
        return searchElasticSearch(videoQuery = videoQuery, startIndex = 0, windowSize = 1).totalHits
    }

    private fun searchElasticSearch(videoQuery: VideoQuery, startIndex: Int, windowSize: Int): SearchHits {
        val esQuery = if (isIdLookup(videoQuery)) {
            buildIdLookupRequest(videoQuery.ids)
        } else {
            buildFuzzyRequest(videoQuery)
        }

        val request = SearchRequest(
            arrayOf(ElasticSearchIndex.ES_INDEX_ALIAS),
            esQuery
                .from(startIndex)
                .size(windowSize)
        )
        return client.search(request, RequestOptions.DEFAULT).hits
    }

    private fun buildFuzzyRequest(videoQuery: VideoQuery): SearchSourceBuilder {
        val esQuery = SearchSourceBuilder()
            .query(fuzzyQuery(videoQuery))

        if (videoQuery.sort === null) {
            esQuery.addRescorer(rescorer(videoQuery.phrase))
        } else {
            esQuery.sort(videoQuery.sort.fieldName.name, SortOrder.fromString(videoQuery.sort.order.toString()))
        }

        return esQuery
    }

    private fun buildIdLookupRequest(ids: List<String>): SearchSourceBuilder {
        return SearchSourceBuilder()
            .query(findMatchesById(ids))
    }

    private fun findMatchesById(ids: List<String>) = idQuery(QueryBuilders.idsQuery().addIds(*(ids.toTypedArray())))

    private fun idQuery(findMatchesById: IdsQueryBuilder?): BoolQueryBuilder {
        return QueryBuilders.boolQuery().should(findMatchesById)
    }

    private fun fuzzyQuery(videoQuery: VideoQuery): BoolQueryBuilder? {
        return QueryBuilders
            .boolQuery()
            .should(matchFieldsExceptContentPartner(videoQuery))
            .should(matchContentPartnerAndTagsExactly(videoQuery).boost(1000.0F))
    }

    private fun matchContentPartnerAndTagsExactly(videoQuery: VideoQuery): BoolQueryBuilder {
        return QueryBuilders
            .boolQuery()
            .must(
                QueryBuilders.termQuery(
                    ElasticSearchVideo.CONTENT_PROVIDER,
                    videoQuery.phrase
                )
            )
            .mustNot(matchTags(videoQuery.excludeTags))
            .filter(filterByTag(videoQuery.includeTags))
    }

    private fun matchFieldsExceptContentPartner(videoQuery: VideoQuery): BoolQueryBuilder {
        return QueryBuilders
            .boolQuery()
            .must(matchTitleDescriptionKeyword(videoQuery.phrase))
            .apply {
                if (listOfNotNull(videoQuery.minDuration, videoQuery.maxDuration).isNotEmpty()) {
                    must(beWithinDuration(videoQuery.minDuration, videoQuery.maxDuration))
                }
            }.apply {
                if (videoQuery.source != null) {
                    filter(matchSource(videoQuery.source))
                }
            }.apply {
                if (listOfNotNull(videoQuery.releaseDateFrom, videoQuery.releaseDateTo).isNotEmpty()) {
                    must(beWithinReleaseDate(videoQuery.releaseDateFrom, videoQuery.releaseDateTo))
                }
            }
            .should(boostTitleMatch(videoQuery.phrase))
            .should(boostDescriptionMatch(videoQuery.phrase))
            .mustNot(matchTags(videoQuery.excludeTags))
            .filter(filterByTag(videoQuery.includeTags))
    }

    private fun matchSource(source: SourceType): TermQueryBuilder {
        return QueryBuilders.termQuery(
            ElasticSearchVideo.SOURCE,
            source.name.toLowerCase()
        )
    }

    private fun beWithinDuration(min: Duration?, max: Duration?): RangeQueryBuilder {
        val queryBuilder = QueryBuilders.rangeQuery(ElasticSearchVideo.DURATION_SECONDS)

        min?.let { queryBuilder.from(it.seconds) }
        max?.let { queryBuilder.to(it.seconds) }

        return queryBuilder
    }

    private fun beWithinReleaseDate(from: LocalDate?, to: LocalDate?): RangeQueryBuilder {
        val queryBuilder = QueryBuilders.rangeQuery(ElasticSearchVideo.RELEASE_DATE)

        from?.let { queryBuilder.from(it) }
        to?.let { queryBuilder.to(it) }

        return queryBuilder
    }

    private fun matchTags(excludeTags: List<String>) =
        QueryBuilders.termsQuery(ElasticSearchVideo.TAGS, excludeTags)

    private fun filterByTag(includeTags: List<String>): BoolQueryBuilder? {
        return includeTags
            .fold(QueryBuilders.boolQuery()) { acc: BoolQueryBuilder, term: String ->
                acc.must(QueryBuilders.termQuery(ElasticSearchVideo.TAGS, term))
            }
    }

    private fun boostDescriptionMatch(phrase: String?): MatchPhraseQueryBuilder {
        return QueryBuilders.matchPhraseQuery(ElasticSearchVideo.DESCRIPTION, phrase)
    }

    private fun boostTitleMatch(phrase: String?): MatchPhraseQueryBuilder {
        return QueryBuilders.matchPhraseQuery(ElasticSearchVideo.TITLE, phrase)
    }

    private fun matchTitleDescriptionKeyword(phrase: String?): MultiMatchQueryBuilder {
        return QueryBuilders
            .multiMatchQuery(
                phrase,
                ElasticSearchVideo.TITLE,
                "${ElasticSearchVideo.TITLE}.std",
                ElasticSearchVideo.DESCRIPTION,
                "${ElasticSearchVideo.DESCRIPTION}.std",
                ElasticSearchVideo.TRANSCRIPT,
                ElasticSearchVideo.KEYWORDS
            )
            .type(MultiMatchQueryBuilder.Type.MOST_FIELDS)
            .minimumShouldMatch("75%")
            .fuzziness(Fuzziness.ZERO)
    }

    private fun rescorer(phrase: String?): QueryRescorerBuilder {

        val rescoreQuery = QueryBuilders.multiMatchQuery(
            phrase,
            "title.$FIELD_DESCRIPTOR_SHINGLES",
            "description.$FIELD_DESCRIPTOR_SHINGLES"
        )
            .type(MultiMatchQueryBuilder.Type.MOST_FIELDS)
        return QueryRescorerBuilder(rescoreQuery)
            .windowSize(100)
            .setScoreMode(QueryRescoreMode.Total)
    }

    private fun isIdLookup(videoQuery: VideoQuery) = videoQuery.ids.isNotEmpty()
}
