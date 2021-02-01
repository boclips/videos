package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.videos.model.DurationRange
import com.boclips.search.service.domain.videos.model.SourceType
import com.boclips.search.service.domain.videos.model.UserQuery
import com.boclips.search.service.infrastructure.common.filters.beWithinAgeRange
import com.boclips.search.service.infrastructure.common.filters.beWithinAgeRanges
import com.boclips.search.service.infrastructure.common.filters.matchAttachmentTypes
import org.elasticsearch.index.query.*
import org.elasticsearch.index.query.QueryBuilders.*
import java.math.BigDecimal
import java.time.LocalDate

class VideoFilterCriteria {
    companion object {
        const val SUBJECTS = "subjects-filter"
        const val AGE_RANGES = "age-ranges-filter"
        const val DURATION_RANGES = "duration-ranges-filter"
        const val ATTACHMENT_TYPES = "attachment-types-filter"
        const val CHANNEL_NAMES_FILTER = "content-provider-filter"
        const val CHANNEL_IDS_FILTER = "content-partner-id-filter"
        const val VIDEO_TYPES_FILTER = "video-types-filter"
        const val VIDEO_PRICES_FILTER = "video-prices-filter"

        fun allCriteria(videoQuery: UserQuery): BoolQueryBuilder {
            val query = boolQuery()
            if (videoQuery.channelNames.isNotEmpty()) {
                query.filter(
                    boolQuery().queryName(CHANNEL_NAMES_FILTER).must(
                        termsQuery(
                            VideoDocument.CONTENT_PROVIDER,
                            videoQuery.channelNames
                        )
                    )
                )
            }

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

            if (!videoQuery.organisationPriceFilter.first.isNullOrEmpty() && videoQuery.organisationPriceFilter.second.isNotEmpty()) {
                query.must(matchPrices(videoQuery.organisationPriceFilter))
            }

            videoQuery.subjectsSetManually?.let { subjectsSetManually ->
                query.must(matchSubjectsSetManually(subjectsSetManually))
            }

            return query
        }

        fun removeCriteria(queryBuilder: BoolQueryBuilder, filterName: String): BoolQueryBuilder {
            fun removeFromList(must: MutableList<QueryBuilder>) {
                val filter = must.find { it.queryName() === filterName }
                filter?.let { must.remove(it) }
            }

            queryBuilder
                .apply {
                    removeFromList(this.must() ?: mutableListOf())
                    removeFromList(this.should() ?: mutableListOf())
                    removeFromList(this.filter() ?: mutableListOf())
                }

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
                queries.should(QueryBuilders.matchPhraseQuery(VideoDocument.SUBJECT_IDS, s))
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

        private fun matchPrices(organisationPriceFilter: Pair<String?, Set<BigDecimal>>): BoolQueryBuilder {
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
