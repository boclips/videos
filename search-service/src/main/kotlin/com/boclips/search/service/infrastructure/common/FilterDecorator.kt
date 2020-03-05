package com.boclips.search.service.infrastructure.common

import com.boclips.search.service.domain.collections.model.CollectionQuery
import com.boclips.search.service.domain.videos.model.AgeRange
import com.boclips.search.service.domain.videos.model.DurationRange
import com.boclips.search.service.domain.videos.model.SourceType
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.infrastructure.videos.VideoDocument
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.index.query.RangeQueryBuilder
import org.elasticsearch.index.query.TermQueryBuilder
import java.time.LocalDate

class FilterDecorator(private val boolQueryBuilder: BoolQueryBuilder) {
    fun apply(videoQuery: VideoQuery) {
        attachFilters(videoQuery)
    }

    fun apply(collectionQuery: CollectionQuery) {
        attachFilters(collectionQuery)
    }

    private fun attachFilters(collectionQuery: CollectionQuery) {
        if (listOfNotNull(collectionQuery.ageRangeMin, collectionQuery.ageRangeMax).isNotEmpty()) {
            boolQueryBuilder.filter(beWithinAgeRange(collectionQuery.ageRangeMin, collectionQuery.ageRangeMax))
        }
    }

    private fun attachFilters(videoQuery: VideoQuery) {
        if (videoQuery.bestFor != null) {
            boolQueryBuilder.filter(filterByTag(videoQuery.bestFor))
        }
        if (videoQuery.durationRanges?.isNotEmpty() == true) {
            boolQueryBuilder.must(beWithinDurationRanges(videoQuery.durationRanges))
        }
        if (videoQuery.source != null) {
            boolQueryBuilder.filter(matchSource(videoQuery.source))
        }
        if (listOfNotNull(videoQuery.releaseDateFrom, videoQuery.releaseDateTo).isNotEmpty()) {
            boolQueryBuilder.must(beWithinReleaseDate(videoQuery.releaseDateFrom, videoQuery.releaseDateTo))
        }
        if (listOfNotNull(videoQuery.ageRangeMin, videoQuery.ageRangeMax).isNotEmpty()) {
            boolQueryBuilder.filter(beWithinAgeRange(videoQuery.ageRangeMin, videoQuery.ageRangeMax))
        }
        if (!videoQuery.ageRanges.isNullOrEmpty()) {
            boolQueryBuilder.filter(beWithinAgeRanges(videoQuery.ageRanges))
        }
        if (videoQuery.subjectIds.isNotEmpty()) {
            boolQueryBuilder.must(matchSubjects(videoQuery.subjectIds))
        }
        if (videoQuery.promoted != null) {
            boolQueryBuilder.must(matchPromoted(videoQuery.promoted))
        }
        if (videoQuery.isClassroom != null) {
            boolQueryBuilder.must(matchIsClassroom(videoQuery.isClassroom))
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

    private fun beWithinDurationRanges(durationRanges: List<DurationRange>): BoolQueryBuilder {
        return QueryBuilders.boolQuery().apply {
            durationRanges.forEach { durationRange ->
                should(
                    QueryBuilders.rangeQuery(VideoDocument.DURATION_SECONDS).apply {
                        from(durationRange.min.seconds)
                        durationRange.max?.let { max -> to(max.seconds) }
                    }
                )
            }
            minimumShouldMatch(1)
        }
    }

    private fun beWithinReleaseDate(from: LocalDate?, to: LocalDate?): RangeQueryBuilder {
        val queryBuilder = QueryBuilders.rangeQuery(VideoDocument.RELEASE_DATE)

        from?.let { queryBuilder.from(it) }
        to?.let { queryBuilder.to(it) }

        return queryBuilder
    }

    private fun beWithinAgeRanges(ageRanges: List<AgeRange>): BoolQueryBuilder? {
        return QueryBuilders.boolQuery().apply {
            ageRanges.forEach { ageRange ->
                should(QueryBuilders.termsQuery(HasAgeRange.AGE_RANGE, ageRange.toRange()))
            }
        }
    }

    private fun beWithinAgeRange(filterMin: Int?, filterMax: Int?): BoolQueryBuilder {
        return QueryBuilders
            .boolQuery()
            .apply {
                if (filterMin != null) {
                    must(QueryBuilders.rangeQuery(HasAgeRange.AGE_RANGE_MIN).apply {
                        gte(filterMin)
                        lt(filterMax)
                    })
                }
                if (filterMax != null) {
                    must(QueryBuilders.rangeQuery(HasAgeRange.AGE_RANGE_MAX).apply {
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
