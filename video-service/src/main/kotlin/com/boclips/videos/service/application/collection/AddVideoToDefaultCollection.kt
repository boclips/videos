package com.boclips.videos.service.application.collection

import com.boclips.security.utils.UserExtractor
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.service.AddVideoToCollection
import com.boclips.videos.service.domain.service.CollectionService

class AddVideoToDefaultCollection(
        private val collectionService: CollectionService
) {
    fun execute(videoId: String?) {
        videoId ?: throw Exception("Video id cannot be null")

        val user = UserExtractor.getCurrentUser().id

        if (collectionService.getByOwner(user).isEmpty()) {
            collectionService.create(user)
        }

        val collection = collectionService.getByOwner(user).first()

        collectionService.update(collection.id, AddVideoToCollection(AssetId(videoId)))
    }
}