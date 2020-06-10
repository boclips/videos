package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.videos.model.AgeRange
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.domain.videos.model.VideoType
import com.boclips.search.service.infrastructure.IndexConfiguration
import com.boclips.search.service.infrastructure.common.HasAgeRange
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
        return QueryBuilders.boolQuery()
            .apply {
                if (videoQuery.phrase.isNotBlank()) {
                    must(
                        QueryBuilders.boolQuery()
                            .should(
                                QueryBuilders.matchPhraseQuery(VideoDocument.TITLE, videoQuery.phrase).slop(2)
                                    .boost(10F)
                            )
                            .should(
                                QueryBuilders.matchPhraseQuery(
                                    IndexConfiguration.unstemmed(VideoDocument.TITLE),
                                    videoQuery.phrase
                                ).slop(2).boost(10F)
                            )
                            .should(
                                QueryBuilders.matchPhraseQuery(VideoDocument.DESCRIPTION, videoQuery.phrase).slop(1)
                            )
                            .should(
                                QueryBuilders.multiMatchQuery(
                                    videoQuery.phrase,
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
                                QueryBuilders.termQuery(VideoDocument.CONTENT_PROVIDER, videoQuery.phrase).boost(1000F)
                            )
                            .should(
                                QueryBuilders.matchPhraseQuery(VideoDocument.SUBJECT_NAMES, videoQuery.phrase)
                                    .boost(1000F)
                            )
                            .minimumShouldMatch(1)

                    )
                }

                must(
                    QueryBuilders.boolQuery()
                        .let(boostInstructionalVideos())
                        .let(boostWhenSubjectsMatch(videoQuery.userSubjectIds))
                )

                if (videoQuery.ageRanges != null) {
                    must(
                        QueryBuilders.boolQuery()
                            .let(boostAgeRangeOverlap(videoQuery.ageRanges))
                    )
                }
            }
            .apply {
                permittedIdsFilter(this, videoQuery.ids, videoQuery.permittedVideoIds)
            }
    }

    private fun permittedIdsFilter(
        currentQueryBuilder: BoolQueryBuilder,
        idsToLookup: Collection<String>,
        permittedVideoIds: Collection<String>?
    ): BoolQueryBuilder {
        val ids = permittedVideoIds
            ?.takeUnless { it.isNullOrEmpty() }
            ?.let {
                if (idsToLookup.isNotEmpty()) {
                    idsToLookup.intersect(permittedVideoIds)
                } else {
                    permittedVideoIds
                }
            }
            ?: idsToLookup

        if (ids.isNotEmpty()) {
            currentQueryBuilder.must(
                QueryBuilders.boolQuery().must(
                    QueryBuilders.idsQuery().addIds(*(ids.toTypedArray()))
                )
            )
        }

        return currentQueryBuilder
    }

    private fun boostInstructionalVideos(): (QueryBuilder) -> BoostingQueryBuilder = { innerQuery: QueryBuilder ->
        QueryBuilders.boostingQuery(
            innerQuery,
            QueryBuilders.boolQuery().mustNot(QueryBuilders.termQuery(VideoDocument.TYPES, VideoType.INSTRUCTIONAL.name))
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