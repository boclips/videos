package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.ReadSearchService
import com.boclips.search.service.domain.model.PaginatedSearchRequest
import com.boclips.search.service.domain.videos.model.SourceType
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.infrastructure.IndexConfiguration.Companion.FIELD_DESCRIPTOR_SHINGLES
import mu.KLogging
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.client.RequestOptions
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

class ESVideoReadSearchService(val client: RestHighLevelClient) :
    ReadSearchService<VideoMetadata, VideoQuery> {
    companion object : KLogging();

    private val elasticSearchResultConverter =
        ESVideoConverter()

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
            arrayOf(ESVideosIndex.getIndexAlias()),
            esQuery
                .from(startIndex)
                .size(windowSize)
        )
        return client.search(request, RequestOptions.DEFAULT).hits
    }

    private fun buildFuzzyRequest(videoQuery: VideoQuery): SearchSourceBuilder {
        val esQuery = SearchSourceBuilder().query(fuzzyQuery(videoQuery))

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
            .apply {
                should(matchContentPartnerAndTagsExactly(videoQuery).boost(1000.0F))
            }
            .apply {
                if (videoQuery.phrase.isEmpty()) {
                    must(matchFieldsExceptContentPartner(videoQuery))
                }
                if (videoQuery.phrase.isNotEmpty()) {
                    should(matchFieldsExceptContentPartner(videoQuery))
                }
            }
    }

    private fun matchContentPartnerAndTagsExactly(videoQuery: VideoQuery): BoolQueryBuilder {
        return QueryBuilders
            .boolQuery()
            .apply {
                must(QueryBuilders.termQuery(ESVideo.CONTENT_PROVIDER, videoQuery.phrase))
            }
            .apply {
                mustNot(matchTags(videoQuery.excludeTags))
            }
            .apply {
                filter(filterByTag(videoQuery.includeTags))
            }
    }

    private fun matchFieldsExceptContentPartner(videoQuery: VideoQuery): BoolQueryBuilder {
        return QueryBuilders
            .boolQuery()
            .apply {
                if (videoQuery.phrase.isNotEmpty()) {
                    must(matchTitleDescriptionKeyword(videoQuery.phrase))
                    should(boostTitleMatch(videoQuery.phrase))
                    should(boostDescriptionMatch(videoQuery.phrase))
                }
            }
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
            }.apply {
                if (listOfNotNull(videoQuery.ageRangeMin, videoQuery.ageRangeMax).isNotEmpty()) {
                    must(beWithinAgeRange(videoQuery.ageRangeMin, videoQuery.ageRangeMax))
                }
            }
            .apply {
                if (videoQuery.subjects.isNotEmpty()) {
                    must(matchSubjects(videoQuery.subjects))
                }
            }
            .apply {
                mustNot(matchTags(videoQuery.excludeTags))
            }
            .apply {
                filter(filterByTag(videoQuery.includeTags))
            }
    }

    private fun matchSubjects(subjects: Set<String>): BoolQueryBuilder? {
        val queries = QueryBuilders.boolQuery()
        for (s: String in subjects) {
            queries.should(QueryBuilders.matchPhraseQuery(ESVideo.SUBJECTS, s))
        }
        return queries
    }

    private fun matchSource(source: SourceType): TermQueryBuilder {
        return QueryBuilders.termQuery(
            ESVideo.SOURCE,
            source.name.toLowerCase()
        )
    }

    private fun beWithinDuration(min: Duration?, max: Duration?): RangeQueryBuilder {
        val queryBuilder = QueryBuilders.rangeQuery(ESVideo.DURATION_SECONDS)

        min?.let { queryBuilder.from(it.seconds) }
        max?.let { queryBuilder.to(it.seconds) }

        return queryBuilder
    }

    private fun beWithinReleaseDate(from: LocalDate?, to: LocalDate?): RangeQueryBuilder {
        val queryBuilder = QueryBuilders.rangeQuery(ESVideo.RELEASE_DATE)

        from?.let { queryBuilder.from(it) }
        to?.let { queryBuilder.to(it) }

        return queryBuilder
    }

    private fun beWithinAgeRange(min: Int?, max: Int?): BoolQueryBuilder {
        return QueryBuilders
            .boolQuery()
            .apply {
                if (min != null) {
                    should(
                        QueryBuilders.boolQuery().apply {
                            must(QueryBuilders.rangeQuery(ESVideo.AGE_RANGE_MIN).apply {
                                to(min)
                            })
                            must(QueryBuilders.rangeQuery(ESVideo.AGE_RANGE_MAX).apply {
                                from(min)
                            })
                        }
                    )

                    should(
                        QueryBuilders.boolQuery().apply {
                            must(QueryBuilders.rangeQuery(ESVideo.AGE_RANGE_MIN).apply {
                                from(min)
                            })
                            max?.let {
                                must(QueryBuilders.rangeQuery(ESVideo.AGE_RANGE_MIN).apply {
                                    to(max)
                                })
                            }
                        }
                    )
                }
            }
    }

    private fun matchTags(excludeTags: List<String>) =
        QueryBuilders.termsQuery(ESVideo.TAGS, excludeTags)

    private fun filterByTag(includeTags: List<String>): BoolQueryBuilder? {
        return includeTags
            .fold(QueryBuilders.boolQuery()) { acc: BoolQueryBuilder, term: String ->
                acc.must(QueryBuilders.termQuery(ESVideo.TAGS, term))
            }
    }

    private fun boostDescriptionMatch(phrase: String?): MatchPhraseQueryBuilder {
        return QueryBuilders.matchPhraseQuery(ESVideo.DESCRIPTION, phrase)
    }

    private fun boostTitleMatch(phrase: String?): MatchPhraseQueryBuilder {
        return QueryBuilders.matchPhraseQuery(ESVideo.TITLE, phrase)
    }

    private fun matchTitleDescriptionKeyword(phrase: String?): MultiMatchQueryBuilder {
        return QueryBuilders
            .multiMatchQuery(
                phrase,
                ESVideo.TITLE,
                "${ESVideo.TITLE}.std",
                ESVideo.DESCRIPTION,
                "${ESVideo.DESCRIPTION}.std",
                ESVideo.TRANSCRIPT,
                ESVideo.KEYWORDS
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
