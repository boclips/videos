package com.boclips.videos.service.application.collection

import com.boclips.security.utils.UserExtractor
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.presentation.collections.CollectionResource
import com.boclips.videos.service.presentation.collections.CollectionResourceConverter
import getCurrentUserId

class GetDefaultCollection(
    private val collectionService: CollectionService,
    private val collectionResourceConverter: CollectionResourceConverter
) {
    operator fun invoke(): CollectionResource {
        val owner = UserExtractor.getCurrentUserId()
        val collection =
            collectionService.getByOwner(owner).firstOrNull() ?:
            collectionService.create(owner = owner, title = Collection.DEFAULT_TITLE)

        return collection.let(collectionResourceConverter::toResource)
    }
}