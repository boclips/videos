package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.videos.model.AgeRange
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.domain.videos.model.VideoType
import com.boclips.search.service.infrastructure.IndexConfiguration
import com.boclips.search.service.infrastructure.common.HasAgeRange
import com.boclips.search.service.infrastructure.videos.AccessRulesFilter.Companion.buildAccessRulesFilter
import org.elasticsearch.common.unit.Fuzziness
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.BoostingQueryBuilder
import org.elasticsearch.index.query.MultiMatchQueryBuilder
import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.index.query.TermsSetQueryBuilder
import org.elasticsearch.script.Script
import org.elasticsearch.script.Script.DEFAULT_SCRIPT_LANG
import org.elasticsearch.script.ScriptType

class EsVideoQuery {
    fun buildQuery(videoQuery: VideoQuery): BoolQueryBuilder {
        val quotation = videoQuery.phrase.quotedParts()
        return QueryBuilders.boolQuery()
            .apply {
                buildAccessRulesFilter(this, videoQuery.videoAccessRuleQuery)
            }
            .apply {
                filter(
                    QueryBuilders.boolQuery().let { filterQuery ->
                        quotation.quotedParts.forEach { phrase ->
                            filterQuery.must(
                                QueryBuilders.boolQuery()
                                    .should(
                                        QueryBuilders.matchPhraseQuery(
                                            IndexConfiguration.unstemmed(VideoDocument.TITLE),
                                            phrase
                                        )
                                    )
                                    .should(
                                        QueryBuilders.matchPhraseQuery(
                                            IndexConfiguration.unstemmed(VideoDocument.DESCRIPTION),
                                            phrase
                                        )
                                    )
                                    .should(
                                        QueryBuilders.matchPhraseQuery(
                                            IndexConfiguration.unstemmed(VideoDocument.TRANSCRIPT),
                                            phrase
                                        )
                                    )
                            )
                        }
                        filterQuery
                    }
                )
            }
            .apply {
                if (quotation.unquoted.isNotBlank()) {
                    must(
                        QueryBuilders.boolQuery()
                            .should(
                                QueryBuilders.matchPhraseQuery(VideoDocument.TITLE, quotation.unquoted).slop(2)
                                    .boost(10F)
                            )
                            .should(
                                QueryBuilders.matchPhraseQuery(
                                    IndexConfiguration.unstemmed(VideoDocument.TITLE),
                                    quotation.unquoted
                                ).slop(2).boost(10F)
                            )
                            .should(
                                QueryBuilders.matchPhraseQuery(VideoDocument.DESCRIPTION, quotation.unquoted).slop(1)
                            )
                            .should(
                                QueryBuilders.multiMatchQuery(
                                    quotation.unquoted,
                                    VideoDocument.TITLE,
                                    IndexConfiguration.unstemmed(VideoDocument.TITLE),
                                    VideoDocument.DESCRIPTION,
                                    IndexConfiguration.unstemmed(VideoDocument.DESCRIPTION),
                                    VideoDocument.TRANSCRIPT,
                                    IndexConfiguration.unstemmed(VideoDocument.TRANSCRIPT),
                                    VideoDocument.KEYWORDS
                                )
                                    .type(MultiMatchQueryBuilder.Type.MOST_FIELDS)
                                    .minimumShouldMatch("75%")
                                    .fuzziness(Fuzziness.ZERO)
                            )
                            .should(
                                QueryBuilders.termQuery(VideoDocument.CONTENT_PROVIDER, quotation.unquoted).boost(1000F)
                            )
                            .should(
                                QueryBuilders.matchPhraseQuery(VideoDocument.SUBJECT_NAMES, quotation.unquoted)
                                    .boost(1000F)
                            )
                            .minimumShouldMatch(1)
                    )
                }

                must(
                    QueryBuilders.boolQuery()
                        .let(boostInstructionalVideos())
                        .let(boostWhenSubjectsMatch(videoQuery.userQuery.userSubjectIds))
                )

                if (videoQuery.userQuery.ageRanges != null) {
                    must(
                        QueryBuilders.boolQuery()
                            .let(boostAgeRangeOverlap(videoQuery.userQuery.ageRanges))
                    )
                }
            }
            .apply {
                idFilter(this, videoQuery.userQuery.ids)
            }
    }

    private fun idFilter(
        currentQueryBuilder: BoolQueryBuilder,
        idsToLookup: Collection<String>
    ): BoolQueryBuilder {
        if (idsToLookup.isNotEmpty()) {
            currentQueryBuilder.must(
                QueryBuilders.boolQuery().must(
                    QueryBuilders.idsQuery().addIds(*(idsToLookup.toTypedArray()))
                )
            )
        }

        return currentQueryBuilder
    }

    private fun boostInstructionalVideos(): (QueryBuilder) -> BoostingQueryBuilder = { innerQuery: QueryBuilder ->
        QueryBuilders.boostingQuery(
            innerQuery,
            QueryBuilders.boolQuery()
                .mustNot(QueryBuilders.termsQuery(VideoDocument.TYPES, VideoType.INSTRUCTIONAL.name))
        ).negativeBoost(0.4F)
    }

    private fun boostAgeRangeOverlap(ageRanges: List<AgeRange>): (QueryBuilder) -> BoostingQueryBuilder =
        { innerQuery: QueryBuilder ->
            QueryBuilders.boostingQuery(
                innerQuery,
                ageRanges.fold(
                    QueryBuilders.boolQuery(),
                    { q, ageRange ->
                        q.mustNot(
                            TermsSetQueryBuilder(HasAgeRange.AGE_RANGE, ageRange.toRange()).setMinimumShouldMatchScript(
                                Script(ScriptType.INLINE, DEFAULT_SCRIPT_LANG, "2", emptyMap())
                            )
                        )
                    })
            ).negativeBoost(0.5F)
        }

    private fun boostWhenSubjectsMatch(subjectIds: Set<String>): (QueryBuilder) -> BoostingQueryBuilder {
        return { innerQuery: QueryBuilder ->
            QueryBuilders.boostingQuery(
                innerQuery,
                subjectIds.fold(
                    QueryBuilders.boolQuery(),
                    { q, subjectId ->
                        q.mustNot(
                            QueryBuilders.matchPhraseQuery(
                                VideoDocument.SUBJECT_IDS,
                                subjectId
                            )
                        )
                    })
            ).negativeBoost(0.5F)
        }
    }
}
