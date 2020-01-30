package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.videos.model.SourceType
import com.boclips.search.service.domain.videos.model.VideoQuery
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.index.query.RangeQueryBuilder
import org.elasticsearch.index.query.TermQueryBuilder
import java.time.Duration
import java.time.LocalDate

class FilterDecorator(private val existingQuery: BoolQueryBuilder) {
    fun apply(videoQuery: VideoQuery) {
        attachFilters(videoQuery)
    }

    private fun attachFilters(videoQuery: VideoQuery) {
        if (videoQuery.bestFor != null) {
            existingQuery.filter(filterByTag(videoQuery.bestFor))
        }
        if (listOfNotNull(videoQuery.minDuration, videoQuery.maxDuration).isNotEmpty()) {
            existingQuery.must(beWithinDuration(videoQuery.minDuration, videoQuery.maxDuration))
        }
        if (videoQuery.source != null) {
            existingQuery.filter(matchSource(videoQuery.source))
        }
        if (listOfNotNull(videoQuery.releaseDateFrom, videoQuery.releaseDateTo).isNotEmpty()) {
            existingQuery.must(beWithinReleaseDate(videoQuery.releaseDateFrom, videoQuery.releaseDateTo))
        }
        if (listOfNotNull(videoQuery.ageRangeMin, videoQuery.ageRangeMax).isNotEmpty()) {
            existingQuery.must(beWithinAgeRange(videoQuery.ageRangeMin, videoQuery.ageRangeMax))
        }
        if (videoQuery.subjectIds.isNotEmpty()) {
            existingQuery.must(matchSubjects(videoQuery.subjectIds))
        }
        if (videoQuery.promoted != null) {
            existingQuery.must(matchPromoted(videoQuery.promoted))
        }
        if (videoQuery.isClassroom != null) {
            existingQuery.must(matchIsClassroom(videoQuery.isClassroom))
        }
    }

    private fun matchSubjects(subjects: Set<String>): BoolQueryBuilder? {
        val queries = QueryBuilders.boolQuery()
        for (s: String in subjects) {
            queries.should(QueryBuilders.matchPhraseQuery(VideoDocument.SUBJECT_IDS, s))
        }
        return queries
    }

    private fun matchPromoted(promoted: Boolean): TermQueryBuilder {
        return QueryBuilders.termQuery(
            VideoDocument.PROMOTED,
            promoted
        )
    }

    private fun matchIsClassroom(isClassroom: Boolean): TermQueryBuilder {
        return QueryBuilders.termQuery(
            VideoDocument.IS_CLASSROOM,
            isClassroom
        )
    }

    private fun matchSource(source: SourceType): TermQueryBuilder {
        return QueryBuilders.termQuery(
            VideoDocument.SOURCE,
            source.name.toLowerCase()
        )
    }

    private fun beWithinDuration(min: Duration?, max: Duration?): RangeQueryBuilder {
        val queryBuilder = QueryBuilders.rangeQuery(VideoDocument.DURATION_SECONDS)

        min?.let { queryBuilder.from(it.seconds) }
        max?.let { queryBuilder.to(it.seconds) }

        return queryBuilder
    }

    private fun beWithinReleaseDate(from: LocalDate?, to: LocalDate?): RangeQueryBuilder {
        val queryBuilder = QueryBuilders.rangeQuery(VideoDocument.RELEASE_DATE)

        from?.let { queryBuilder.from(it) }
        to?.let { queryBuilder.to(it) }

        return queryBuilder
    }

    private fun beWithinAgeRange(filterMin: Int?, filterMax: Int?): BoolQueryBuilder {
        return QueryBuilders
            .boolQuery()
            .apply {
                if (filterMin != null) {
                    must(QueryBuilders.rangeQuery(VideoDocument.AGE_RANGE_MIN).apply {
                        gte(filterMin)
                        lt(filterMax)
                    })
                }
                if (filterMax != null) {
                    must(QueryBuilders.rangeQuery(VideoDocument.AGE_RANGE_MAX).apply {
                        gt(filterMin)
                        lte(filterMax)
                    })
                }

            }
    }

    private fun filterByTag(includeTags: List<String>): BoolQueryBuilder? {
        return includeTags
            .fold(QueryBuilders.boolQuery()) { acc: BoolQueryBuilder, term: String ->
                acc.must(QueryBuilders.termQuery(VideoDocument.TAGS, term))
            }
    }
}
