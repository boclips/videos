package com.boclips.videos.service.application.collection

import com.boclips.videos.service.common.Page
import com.boclips.videos.service.common.PageInfo
import com.boclips.videos.service.domain.model.User
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.presentation.CollectionsController
import com.boclips.videos.service.presentation.Projection
import com.boclips.videos.service.presentation.collections.CollectionResource
import com.boclips.videos.service.presentation.collections.CollectionResourceFactory

class GetCollections(
    private val collectionService: CollectionService,
    private val collectionResourceFactory: CollectionResourceFactory,
    private val collectionSearchQueryAssembler: CollectionSearchQueryAssembler
) {
    operator fun invoke(
        collectionsRequest: CollectionsController.CollectionsRequest,
        user: User
    ): Page<CollectionResource> {
        val collections = getUnassembledCollections(collectionsRequest, user)

        return assembleResourcesPage(
            projection = collectionsRequest.projection,
            pageInfo = collections.pageInfo,
            collections = collections.elements,
            user = user
        )
    }

    fun getUnassembledCollections(
        collectionsRequest: CollectionsController.CollectionsRequest,
        user: User
    ): Page<Collection> {
        val assembledQuery = collectionSearchQueryAssembler(
            query = collectionsRequest.query,
            subjects = collectionsRequest.subjects,
            public = collectionsRequest.public,
            bookmarked = collectionsRequest.bookmarked ?: false,
            owner = collectionsRequest.owner,
            page = collectionsRequest.page,
            size = collectionsRequest.size,
            user = user
        )

        return collectionService.search(assembledQuery)
    }

    private fun assembleResourcesPage(
        projection: Projection,
        pageInfo: PageInfo,
        collections: Iterable<Collection>,
        user: User
    ): Page<CollectionResource> {
        return Page(collections.map {
            collectionResourceFactory.buildCollectionResource(it, projection, user)
        }, pageInfo)
    }
}
