package com.boclips.videos.service.application.collection

import com.boclips.videos.service.application.collection.security.getOwnedCollectionOrThrow
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.domain.service.collection.CollectionUpdateCommand
import com.boclips.videos.service.infrastructure.analytics.AnalyticsEventService

class AddVideoToCollection(
    private val collectionService: CollectionService,
    private val analyticsEventService: AnalyticsEventService
) {
    operator fun invoke(collectionId: String?, videoId: String?) {
        collectionId ?: throw Exception("Collection id cannot be null")
        videoId ?: throw Exception("Video id cannot be null")

        collectionService.getOwnedCollectionOrThrow(collectionId)

        collectionService.update(
            CollectionId(collectionId),
            CollectionUpdateCommand.AddVideoToCollectionCommand(AssetId(videoId))
        )

        analyticsEventService.saveUpdateCollectionEvent(CollectionId(collectionId), listOf(
            CollectionUpdateCommand.AddVideoToCollectionCommand(
                AssetId(videoId)
            )
        ))
    }
}