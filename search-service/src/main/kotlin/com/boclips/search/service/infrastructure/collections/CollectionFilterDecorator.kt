package com.boclips.search.service.infrastructure.collections

import com.boclips.search.service.domain.collections.model.CollectionQuery
import com.boclips.search.service.domain.collections.model.CollectionVisibility
import com.boclips.search.service.domain.collections.model.CollectionVisibilityQuery
import com.boclips.search.service.infrastructure.common.filters.beWithinAgeRange
import com.boclips.search.service.infrastructure.common.filters.beWithinAgeRanges
import mu.KLogging
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.QueryBuilders

class CollectionFilterDecorator(private val boolQueryBuilder: BoolQueryBuilder) {
    companion object : KLogging()

    fun decorate(collectionQuery: CollectionQuery) {
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
        if (collectionQuery.hasLessonPlans != null) {
            boolQueryBuilder.filter(
                QueryBuilders.termsQuery(
                    CollectionDocument.HAS_LESSON_PLANS,
                    collectionQuery.hasLessonPlans
                )
            )
        }
        if (collectionQuery.promoted != null) {
            boolQueryBuilder.filter(
                QueryBuilders.termsQuery(
                    CollectionDocument.PROMOTED,
                    collectionQuery.promoted
                )
            )
        }

        if (collectionQuery.permittedIds != null) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery(CollectionDocument.ID, collectionQuery.permittedIds))
        }
        if (collectionQuery.subjectIds.isNotEmpty()) {
            boolQueryBuilder.filter(matchSubjects(collectionQuery.subjectIds))
        }
        boolQueryBuilder.filter(
            QueryBuilders.boolQuery().apply {
                collectionQuery.visibilityForOwners.forEach { visibilityForOwner ->
                    should(
                        QueryBuilders.boolQuery().apply {
                            visibilityForOwner.owner?.let {
                                must(QueryBuilders.termQuery(CollectionDocument.OWNER, it))
                            }
                            must(
                                QueryBuilders.termsQuery(
                                    CollectionDocument.VISIBILITY,
                                    when (visibilityForOwner.visibility) {
                                        CollectionVisibilityQuery.All -> listOf("public", "private")
                                        is CollectionVisibilityQuery.One -> when (visibilityForOwner.visibility.collectionVisibility) {
                                            CollectionVisibility.PUBLIC -> listOf("public")
                                            CollectionVisibility.PRIVATE -> listOf("private")
                                        }
                                    }
                                )
                            )
                        }
                    )
                }

                if (collectionQuery.bookmarkedBy != null) {
                    should(
                        QueryBuilders.termQuery(
                            CollectionDocument.BOOKMARKED_BY,
                            collectionQuery.bookmarkedBy
                        )
                    )
                }

                if (collectionQuery.bookmarkedBy != null && collectionQuery.visibilityForOwners.any { it.owner == null }) {
                    minimumShouldMatch(2)
                }
            }
        )
    }

    private fun matchSubjects(subjects: List<String>): BoolQueryBuilder {
        val queries = QueryBuilders.boolQuery()
        for (subject: String in subjects) {
            queries.should(QueryBuilders.matchPhraseQuery(CollectionDocument.SUBJECTS, subject))
        }
        return queries
    }
}
