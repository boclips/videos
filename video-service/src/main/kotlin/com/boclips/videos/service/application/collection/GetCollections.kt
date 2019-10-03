package com.boclips.videos.service.application.collection

import com.boclips.videos.service.common.Page
import com.boclips.videos.service.common.PageInfo
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.service.AccessRule
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.presentation.CollectionsController
import com.boclips.videos.service.presentation.Projection
import com.boclips.videos.service.presentation.collections.CollectionResource
import com.boclips.videos.service.presentation.collections.CollectionResourceFactory

class GetCollections(
    private val collectionService: CollectionService,
    private val collectionResourceFactory: CollectionResourceFactory,
    private val collectionFilterAssembler: CollectionSearchQueryAssembler
    ) {
    operator fun invoke(
        collectionsRequest: CollectionsController.CollectionsRequest,
        accessRule: AccessRule
    ): Page<CollectionResource> {
        return getUnassembledCollections(collectionsRequest, accessRule).let {
            assembleResourcesPage(
                projection = collectionsRequest.projection,
                pageInfo = it.pageInfo,
                collections = it.elements
            )
        }
    }

    fun getUnassembledCollections(
        collectionsRequest: CollectionsController.CollectionsRequest,
        accessRule: AccessRule
    ): Page<Collection> {
        return collectionService.search(collectionFilterAssembler(
            query = collectionsRequest.query,
            subjects = collectionsRequest.subjects,
            public = collectionsRequest.public,
            bookmarked = collectionsRequest.bookmarked ?: false,
            owner = collectionsRequest.owner,
            page = collectionsRequest.page,
            size = collectionsRequest.size,
            accessRule = accessRule
        ))
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
