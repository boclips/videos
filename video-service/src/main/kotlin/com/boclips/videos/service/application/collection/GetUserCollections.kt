package com.boclips.videos.service.application.collection

import com.boclips.security.utils.UserExtractor
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.presentation.collections.CollectionResourceConverter
import getCurrentUserId

class GetUserCollections(
    private val collectionService: CollectionService,
    private val collectionResourceConverter: CollectionResourceConverter
) {
    operator fun invoke() = collectionService
        .getByOwner(UserExtractor.getCurrentUserId())
        .map(collectionResourceConverter::toResource)
}