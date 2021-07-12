package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.videos.model.DurationRange
import com.boclips.search.service.domain.videos.model.PricesFilter
import com.boclips.search.service.domain.videos.model.SourceType
import com.boclips.search.service.domain.videos.model.UserQuery
import com.boclips.search.service.infrastructure.common.filters.beWithinAgeRange
import com.boclips.search.service.infrastructure.common.filters.beWithinAgeRanges
import com.boclips.search.service.infrastructure.common.filters.matchAttachmentTypes
import org.elasticsearch.index.query.*
import org.elasticsearch.index.query.QueryBuilders.*
import java.time.LocalDate

class VideoFilterCriteria {
    companion object {
        const val SUBJECTS = "subjects-filter"
        const val CATEGORY_CODES = "category-codes-filter"
        const val AGE_RANGES = "age-ranges-filter"
        const val DURATION_RANGES = "duration-ranges-filter"
        const val ATTACHMENT_TYPES = "attachment-types-filter"
        const val CHANNEL_IDS_FILTER = "content-partner-id-filter"
        const val VIDEO_TYPES_FILTER = "video-types-filter"
        const val VIDEO_PRICES_FILTER = "video-prices-filter"
        const val UPDATED_AT_FROM = "updated-at-from"

        fun allCriteria(videoQuery: UserQuery): BoolQueryBuilder {
            val query = boolQuery()

            if (videoQuery.channelIds.isNotEmpty()) {
                query.filter(
                    boolQuery().queryName(CHANNEL_IDS_FILTER).must(
                        termsQuery(
                            VideoDocument.CONTENT_PARTNER_ID,
                            videoQuery.channelIds
                        )
                    )
                )
            }

            if (videoQuery.bestFor != null) {
                query.filter(filterByTag(videoQuery.bestFor))
            }

            if (videoQuery.durationRanges?.isNotEmpty() == true) {
                query.must(beWithinDurationRanges(videoQuery.durationRanges))
            }

            if (videoQuery.source != null) {
                query.filter(matchSource(videoQuery.source))
            }

            if (listOfNotNull(videoQuery.releaseDateFrom, videoQuery.releaseDateTo).isNotEmpty()) {
                query.must(beWithinReleaseDate(videoQuery.releaseDateFrom, videoQuery.releaseDateTo))
            }

            if (listOfNotNull(videoQuery.ageRangeMin, videoQuery.ageRangeMax).isNotEmpty()) {
                query.filter(
                    beWithinAgeRange(
                        videoQuery.ageRangeMin,
                        videoQuery.ageRangeMax
                    )
                )
            }

            if (!videoQuery.ageRanges.isNullOrEmpty()) {
                query.filter(
                    beWithinAgeRanges(
                        videoQuery.ageRanges
                    )
                )
            }

            if (videoQuery.subjectIds.isNotEmpty()) {
                query.must(matchSubjects(videoQuery.subjectIds))
            }

            if (videoQuery.promoted != null) {
                query.must(matchPromoted(videoQuery.promoted))
            }

            if (videoQuery.active != null) {
                query.must(matchActive(videoQuery.active))
            }

            if (!videoQuery.attachmentTypes.isNullOrEmpty()) {
                query.must(matchAttachmentTypes(videoQuery.attachmentTypes))
            }

            if (videoQuery.types.isNotEmpty()) {
                query.must(termsQuery(VideoDocument.TYPES, videoQuery.types).queryName(VIDEO_TYPES_FILTER))
            }

            if (videoQuery.organisationPriceFilter.shouldFilter()) {
                query.must(matchPrices(videoQuery.organisationPriceFilter))
            }

            if (videoQuery.categoryCodes.isNotEmpty()) {
                query.must(matchCategoryCodes(videoQuery.categoryCodes))
            }

            videoQuery.subjectsSetManually?.let { subjectsSetManually ->
                query.must(matchSubjectsSetManually(subjectsSetManually))
            }

            return query
        }

        fun removeCriteria(queryBuilder: BoolQueryBuilder, filterName: String): BoolQueryBuilder {
            listOfNotNull(
                queryBuilder.must(),
                queryBuilder.should(),
                queryBuilder.filter()
            ).forEach { clauses -> clauses.removeIf { it.queryName() === filterName } }
            return queryBuilder
        }

        private fun matchSubjectsSetManually(subjectsSetManually: Boolean): TermsQueryBuilder =
            termsQuery(
                VideoDocument.SUBJECTS_SET_MANUALLY,
                subjectsSetManually
            )

        private fun matchSubjects(subjects: Set<String>): BoolQueryBuilder? {
            val queries = boolQuery().queryName(SUBJECTS)
            for (s: String in subjects) {
                queries.should(matchPhraseQuery(VideoDocument.SUBJECT_IDS, s))
            }
            return queries
        }

        private fun matchUpdatedAtFrom(updatedAtFrom: LocalDate): BoolQueryBuilder? {
            return boolQuery().queryName(UPDATED_AT_FROM).apply {
                updatedAtFrom.isBefore(LocalDate.parse(VideoDocument.UPDATED_AT))
            }
        }

        private fun matchCategoryCodes(categoryCodes: Set<String>): BoolQueryBuilder? {
            val queries = boolQuery().queryName(CATEGORY_CODES)
            for (code: String in categoryCodes) {
                queries.should(matchPhraseQuery(VideoDocument.CATEGORY_CODES, code))
            }
            return queries
        }

        private fun matchPromoted(promoted: Boolean): TermQueryBuilder {
            return termQuery(
                VideoDocument.PROMOTED,
                promoted
            )
        }

        private fun matchActive(active: Boolean): TermQueryBuilder {
            return termQuery(
                VideoDocument.DEACTIVATED,
                !active
            )
        }

        private fun matchSource(source: SourceType): TermQueryBuilder {
            return termQuery(
                VideoDocument.SOURCE,
                source.name.toLowerCase()
            )
        }

        private fun beWithinDurationRanges(durationRanges: List<DurationRange>): BoolQueryBuilder {
            return boolQuery()
                .queryName(DURATION_RANGES)
                .apply {
                    durationRanges.forEach { durationRange ->
                        should(
                            QueryBuilders.rangeQuery(VideoDocument.DURATION_SECONDS).apply {
                                from(durationRange.min.seconds)
                                durationRange.max?.let { max -> to(max.seconds, false) }
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

        private fun filterByTag(includeTags: List<String>): BoolQueryBuilder? {
            return includeTags
                .fold(boolQuery()) { acc: BoolQueryBuilder, term: String ->
                    acc.must(termQuery(VideoDocument.TAGS, term))
                }
        }

        private fun matchPrices(organisationPriceFilter: PricesFilter): BoolQueryBuilder {
            val (organisationId, queriedPrices) = organisationPriceFilter
            val priceQueries = boolQuery().queryName(VIDEO_PRICES_FILTER)
            queriedPrices
                .map { it.movePointRight(2).toLong() }
                .forEach { queriedPrice ->
                    priceQueries
                        .should(
                            matchPhraseQuery(
                                "${VideoDocument.PRICES}.$organisationId",
                                queriedPrice
                            )
                        ).should(
                            boolQuery()
                                .must(matchPhraseQuery("${VideoDocument.PRICES}.DEFAULT", queriedPrice))
                                .mustNot(existsQuery("${VideoDocument.PRICES}.$organisationId"))
                        )
                }
            return priceQueries
        }
    }
}
