package com.boclips.videos.service.application.collection

import com.boclips.videos.service.application.getCurrentUserId
import com.boclips.videos.service.common.Page
import com.boclips.videos.service.common.PageRequest
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionRepository

class GetBookmarkedCollections(private val collectionRepository: CollectionRepository) {
    operator fun invoke(collectionFilter: CollectionFilter): Page<Collection> {
        val pageRequest = PageRequest(page = collectionFilter.pageNumber, size = collectionFilter.pageSize)
        return collectionRepository.getBookmarkedByUser(pageRequest, getCurrentUserId())
    }
}