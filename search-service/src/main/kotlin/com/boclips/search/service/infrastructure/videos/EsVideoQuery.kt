package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.domain.videos.model.VideoType
import com.boclips.search.service.infrastructure.IndexConfiguration
import org.elasticsearch.common.unit.Fuzziness
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.BoostingQueryBuilder
import org.elasticsearch.index.query.MultiMatchQueryBuilder
import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.index.query.QueryBuilders

class EsVideoQuery {
    fun buildQuery(videoQuery: VideoQuery): BoolQueryBuilder {
        return QueryBuilders.boolQuery()
            .apply {
                if (videoQuery.phrase.isNotBlank()) {
                    must(
                        QueryBuilders.boolQuery()
                            .should(QueryBuilders.matchPhraseQuery(VideoDocument.TITLE, videoQuery.phrase).slop(1))
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
                            .let(boostInstructionalVideos())
                            .let(boostWhenSubjectsMatch(videoQuery.userSubjectIds))
                    )
                }
            }.apply {
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
            QueryBuilders.boolQuery().mustNot(QueryBuilders.termQuery(VideoDocument.TYPE, VideoType.INSTRUCTIONAL.name))
        ).negativeBoost(0.4F)
    }

    private fun boostWhenSubjectsMatch(subjectIds: Set<String>): (QueryBuilder) -> BoostingQueryBuilder {
        return { innerQuery: QueryBuilder ->
            QueryBuilders.boostingQuery(
                innerQuery,
                subjectIds.fold(
                    QueryBuilders.boolQuery(),
                    { q, subjectId -> q.mustNot(QueryBuilders.matchPhraseQuery(VideoDocument.SUBJECT_IDS, subjectId)) })
            ).negativeBoost(0.5F)
        }
    }
}