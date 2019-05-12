package com.boclips.videos.service.application.collection

import com.boclips.videos.service.application.collection.security.getOwnedCollectionOrThrow
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.collection.CollectionRepository
import com.boclips.videos.service.domain.service.collection.CollectionUpdateCommand
import com.boclips.videos.service.infrastructure.analytics.AnalyticsEventService

class AddVideoToCollection(
    private val collectionRepository: CollectionRepository,
    private val analyticsEventService: AnalyticsEventService
) {
    operator fun invoke(collectionId: String?, videoId: String?) {
        collectionId ?: throw Exception("Collection id cannot be null")
        videoId ?: throw Exception("Video id cannot be null")

        collectionRepository.getOwnedCollectionOrThrow(collectionId)

        collectionRepository.update(
            CollectionId(collectionId),
            CollectionUpdateCommand.AddVideoToCollectionCommand(VideoId(videoId))
        )

        analyticsEventService.saveUpdateCollectionEvent(
            CollectionId(collectionId), listOf(
                CollectionUpdateCommand.AddVideoToCollectionCommand(
                    VideoId(videoId)
                )
            )
        )
    }
}