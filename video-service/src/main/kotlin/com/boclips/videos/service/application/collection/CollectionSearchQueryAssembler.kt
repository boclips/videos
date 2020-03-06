package com.boclips.videos.service.application.collection

import com.boclips.search.service.domain.collections.model.CollectionVisibilityQuery
import com.boclips.search.service.domain.collections.model.VisibilityForOwner
import com.boclips.videos.api.request.collection.CollectionSortKey
import com.boclips.videos.service.application.exceptions.OperationForbiddenException
import com.boclips.videos.service.domain.model.User
import com.boclips.videos.service.domain.model.UserId
import com.boclips.videos.service.domain.model.collection.CollectionAccessRule
import com.boclips.videos.service.domain.model.collection.CollectionSearchQuery
import com.boclips.videos.service.presentation.CollectionsController
import com.boclips.videos.service.presentation.converters.convertAgeRanges
import mu.KLogging

class CollectionSearchQueryAssembler {
    operator fun invoke(
        query: String? = null,
        subjects: List<String>? = null,
        public: Boolean? = null,
        bookmarked: Boolean? = null,
        owner: String? = null,
        page: Int? = null,
        size: Int? = null,
        sort: CollectionSortKey? = null,
        hasLessonPlans: Boolean? = null,
        user: User,
        ageRangeMin: Int? = null,
        ageRangeMax: Int? = null,
        ageRange: List<String>? = null
    ): CollectionSearchQuery {
        val collectionAccess = user.accessRules.collectionAccess

        val bookmarker =
            if (bookmarked == true)
                when (collectionAccess) {
                    is CollectionAccessRule.SpecificOwner -> collectionAccess.owner
                    is CollectionAccessRule.Everything -> user.id
                    else -> throw OperationForbiddenException(
                        "This user cannot have bookmarked collections"
                    )
                } else null

        bookmarker?.let { logger.info("Looking for collections bookmarked by $it") }
        logger.info { "User has collection access: $collectionAccess" }

        return CollectionSearchQuery(
            text = query ?: "",
            subjectIds = subjects ?: emptyList(),
            bookmarkedBy = bookmarker?.value,
            visibilityForOwners = collectionAccess.getVisibility(public = public, owner = owner),
            permittedCollections = when (collectionAccess) {
                is CollectionAccessRule.SpecificIds -> collectionAccess.collectionIds.toList()
                else -> null
            },
            pageSize = size ?: CollectionsController.COLLECTIONS_PAGE_SIZE,
            pageIndex = page ?: 0,
            sort = sort,
            hasLessonPlans = hasLessonPlans,
            ageRangeMin = ageRangeMin,
            ageRangeMax = ageRangeMax,
            ageRanges = ageRange?.map(::convertAgeRanges)
        )
    }

    companion object : KLogging()
}

private fun CollectionAccessRule.getVisibility(public: Boolean?, owner: String?): Set<VisibilityForOwner> =
    when (this) {
        CollectionAccessRule.PublicOnly -> {
            if (public == false) throw OperationForbiddenException("User cannot access private collections")
            setOf(
                VisibilityForOwner(
                    owner = owner,
                    visibility = CollectionVisibilityQuery.publicOnly()
                )
            )
        }
        is CollectionAccessRule.SpecificOwner -> {
            when (owner) {
                null ->
                    when (public) {
                        true -> setOf(
                            VisibilityForOwner(
                                owner = null,
                                visibility = CollectionVisibilityQuery.publicOnly()
                            )
                        )
                        false -> setOf(
                            VisibilityForOwner(
                                owner = this.owner.value,
                                visibility = CollectionVisibilityQuery.privateOnly()
                            )
                        )
                        null -> setOf(
                            VisibilityForOwner(owner = null, visibility = CollectionVisibilityQuery.publicOnly()),
                            VisibilityForOwner(
                                owner = this.owner.value,
                                visibility = CollectionVisibilityQuery.privateOnly()
                            )
                        )
                    }
                else ->
                    setOf(
                        VisibilityForOwner(
                            owner = owner,
                            visibility = when (public) {
                                true -> CollectionVisibilityQuery.publicOnly()
                                false ->
                                    if (this.isMe(owner))
                                        CollectionVisibilityQuery.privateOnly()
                                    else
                                        throw OperationForbiddenException(
                                            "User is not authorized to access private collections of owner with ID $owner"
                                        )
                                null ->
                                    if (this.isMe(owner))
                                        CollectionVisibilityQuery.All
                                    else
                                        CollectionVisibilityQuery.publicOnly()
                            }
                        )
                    )
            }
        }

        is CollectionAccessRule.SpecificIds ->
            allAccess(public, owner)
        CollectionAccessRule.Everything ->
            allAccess(public, owner)
    }

fun allAccess(public: Boolean?, owner: String?) =
    if (public == null && owner == null)
        setOf()
    else
        setOf(
            VisibilityForOwner(
                owner = owner,
                visibility = when (public) {
                    true -> CollectionVisibilityQuery.publicOnly()
                    false -> CollectionVisibilityQuery.privateOnly()
                    null -> CollectionVisibilityQuery.All
                }
            )
        )

fun CollectionAccessRule.SpecificOwner.isMe(userIdValue: String?) =
    userIdValue?.let { this.isMe(UserId(it)) } == true
