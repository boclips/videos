package com.boclips.videos.service.application.collection

import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.collection.CollectionUpdateService

class AddVideoToCollection(private val collectionUpdateService: CollectionUpdateService) {
    operator fun invoke(collectionId: String?, videoId: String?, user: User) {
        collectionId ?: throw Exception("Collection id cannot be null")
        videoId ?: throw Exception("Video id cannot be null")

        collectionUpdateService.addVideoToCollectionOfUser(
            collectionId = CollectionId(collectionId),
            videoId = VideoId(videoId),
            user = user
        )
    }
}
