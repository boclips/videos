package com.boclips.videos.service.application.collection

import com.boclips.security.utils.UserExtractor
import com.boclips.videos.service.application.getCurrentUserId
import com.boclips.videos.service.common.Page
import com.boclips.videos.service.common.PageInfo
import com.boclips.videos.service.domain.model.collection.Collection
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
    private val getBookmarkedCollections: GetBookmarkedCollections,
    private val collectionQueryAssembler: CollectionQueryAssembler
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

        return when {
            userContracts.isNotEmpty() -> getContractedCollections(collectionFilter, userContracts)
            isBookmarkedCollectionsFetch(collectionFilter) -> getBookmarkedCollections(collectionFilter)
            else -> {
                val query = collectionQueryAssembler.assemble(collectionFilter, UserExtractor.getCurrentUser())
                collectionService.search(query)
            }
        }
    }

    private fun isBookmarkedCollectionsFetch(collectionFilter: CollectionFilter) =
        collectionFilter.visibility == CollectionFilter.Visibility.BOOKMARKED

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
