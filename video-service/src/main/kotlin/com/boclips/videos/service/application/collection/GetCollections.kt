package com.boclips.videos.service.application.collection

import com.boclips.search.service.domain.collections.model.CollectionVisibility
import com.boclips.security.utils.UserExtractor.currentUserHasRole
import com.boclips.videos.service.application.exceptions.OperationForbiddenException
import com.boclips.videos.service.application.getCurrentUserId
import com.boclips.videos.service.common.Page
import com.boclips.videos.service.common.PageInfo
import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionSearchQuery
import com.boclips.videos.service.domain.service.UserContractService
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.presentation.Projection
import com.boclips.videos.service.presentation.collections.CollectionResource
import com.boclips.videos.service.presentation.collections.CollectionResourceFactory

class GetCollections(
    private val collectionService: CollectionService,
    private val collectionResourceFactory: CollectionResourceFactory,
    private val getContractedCollections: GetContractedCollections,
    private val userContractService: UserContractService,
    private val getUserPrivateCollections: GetUserPrivateCollections,
    private val getBookmarkedCollections: GetBookmarkedCollections
) {
    operator fun invoke(
        collectionFilter: CollectionFilter,
        projection: Projection
    ): Page<CollectionResource> {
        return getCollections(collectionFilter).let {
            assembleResourcesPage(
                projection = projection,
                pageInfo = it.pageInfo,
                collections = it.elements
            )
        }
    }

    private fun getCollections(collectionFilter: CollectionFilter): Page<Collection> {
        val userContracts = userContractService.getContracts(getCurrentUserId().value)
        val query = CollectionSearchQuery(
            text = collectionFilter.query,
            subjectIds = collectionFilter.subjects,
            visibility = when (collectionFilter.visibility) {
                CollectionFilter.Visibility.PUBLIC -> listOf(CollectionVisibility.PUBLIC)
                CollectionFilter.Visibility.PRIVATE -> listOf(CollectionVisibility.PRIVATE)
                CollectionFilter.Visibility.BOOKMARKED -> listOf(CollectionVisibility.PUBLIC)
                CollectionFilter.Visibility.ALL -> listOf(CollectionVisibility.PRIVATE, CollectionVisibility.PUBLIC)
            },
            pageSize = collectionFilter.pageSize,
            pageIndex = collectionFilter.pageNumber
        )

        return when {
            userContracts.isNotEmpty() -> getContractedCollections(collectionFilter, userContracts)
            isUserPrivateCollectionsFetch(collectionFilter) -> getUserPrivateCollections(collectionFilter)
            isBookmarkedCollectionsFetch(collectionFilter) -> getBookmarkedCollections(collectionFilter)
            isPublicCollectionSearch(collectionFilter) -> searchCollections(query)
            isAllCollectionsSearch(collectionFilter) -> searchCollections(query)
            else -> throw IllegalStateException("Unknown collection lookup method")
        }
    }

    private fun isPublicCollectionSearch(collectionFilter: CollectionFilter) =
        collectionFilter.visibility == CollectionFilter.Visibility.PUBLIC

    private fun isUserPrivateCollectionsFetch(collectionFilter: CollectionFilter) =
        collectionFilter.visibility == CollectionFilter.Visibility.PRIVATE

    private fun isBookmarkedCollectionsFetch(collectionFilter: CollectionFilter) =
        collectionFilter.visibility == CollectionFilter.Visibility.BOOKMARKED

    private fun isAllCollectionsSearch(collectionFilter: CollectionFilter) =
        collectionFilter.visibility == CollectionFilter.Visibility.ALL

    private fun searchCollections(query: CollectionSearchQuery): Page<Collection> {
        if (!(query.visibility.first() === CollectionVisibility.PUBLIC || currentUserHasRole(UserRoles.VIEW_ANY_COLLECTION))) {
            throw OperationForbiddenException()
        }

        return collectionService.search(query)
    }

    private fun assembleResourcesPage(
        projection: Projection,
        pageInfo: PageInfo,
        collections: Iterable<Collection>
    ): Page<CollectionResource> {
        return Page(collections.map {
            collectionResourceFactory.buildCollectionResource(it, projection)
        }, pageInfo)
    }
}
