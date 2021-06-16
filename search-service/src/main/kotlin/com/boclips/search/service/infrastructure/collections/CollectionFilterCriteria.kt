package com.boclips.search.service.infrastructure.collections

import com.boclips.search.service.domain.collections.model.CollectionQuery
import com.boclips.search.service.infrastructure.common.filters.beWithinAgeRange
import com.boclips.search.service.infrastructure.common.filters.beWithinAgeRanges
import com.boclips.search.service.infrastructure.common.filters.matchAttachmentTypes
import mu.KLogging
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.index.query.QueryBuilders.boolQuery

class CollectionFilterCriteria {
    companion object : KLogging() {
        fun allCriteria(collectionQuery: CollectionQuery): BoolQueryBuilder {
            val boolQueryBuilder = boolQuery()

            if (listOfNotNull(collectionQuery.ageRangeMin, collectionQuery.ageRangeMax).isNotEmpty()) {
                boolQueryBuilder.filter(
                    beWithinAgeRange(
                        collectionQuery.ageRangeMin,
                        collectionQuery.ageRangeMax
                    )
                )
            }

            if (!collectionQuery.ageRanges.isNullOrEmpty()) {
                boolQueryBuilder.filter(
                    beWithinAgeRanges(
                        collectionQuery.ageRanges
                    )
                )
            }

            if (collectionQuery.subjectIds.isNotEmpty()) {
                boolQueryBuilder.filter(matchSubjects(collectionQuery.subjectIds))
            }

            if (collectionQuery.resourceTypes.isNotEmpty()) {
                boolQueryBuilder.filter(matchAttachmentTypes(collectionQuery.resourceTypes))
            }

            collectionQuery.permittedIds?.let {
                boolQueryBuilder.filter(QueryBuilders.termsQuery(CollectionDocument.ID, collectionQuery.permittedIds))
            }

            collectionQuery.searchable?.let {
                boolQueryBuilder.filter(boolQuery().must(QueryBuilders.termQuery(CollectionDocument.SEARCHABLE, it)))
            }

            boolQueryBuilder.filter(
                boolQuery().apply {
                    collectionQuery.owner?.let {
                        should(QueryBuilders.termQuery(CollectionDocument.OWNER, it))
                    }

                    collectionQuery.bookmarkedBy?.let {
                        should(QueryBuilders.termQuery(CollectionDocument.BOOKMARKED_BY, it))
                    }
                }
            )

            collectionQuery.hasLessonPlans?.let {
                boolQueryBuilder.filter(
                    QueryBuilders.termsQuery(
                        CollectionDocument.HAS_LESSON_PLANS,
                        collectionQuery.hasLessonPlans
                    )
                )
            }

            collectionQuery.promoted?.let {
                boolQueryBuilder.filter(
                    QueryBuilders.termsQuery(
                        CollectionDocument.PROMOTED,
                        collectionQuery.promoted
                    )
                )
            }

            return boolQueryBuilder
        }

        private fun matchSubjects(subjects: List<String>): BoolQueryBuilder {
            val queries = boolQuery()
            for (subject: String in subjects) {
                queries.should(QueryBuilders.matchPhraseQuery(CollectionDocument.SUBJECTS, subject))
            }
            return queries
        }
    }
}
