package com.boclips.videos.service.application.collection

import com.boclips.videos.service.application.collection.security.getOwnedCollectionOrThrow
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.service.collection.CollectionService
import com.boclips.videos.service.domain.service.collection.RemoveVideoFromCollectionCommand
import com.boclips.videos.service.infrastructure.analytics.AnalyticsEventService

class RemoveVideoFromCollection(
    private val collectionService: CollectionService,
    private val analyticsEventService: AnalyticsEventService
) {
    operator fun invoke(collectionId: String?, videoId: String?) {
        collectionId ?: throw Exception("Collection id cannot be null")
        videoId ?: throw Exception("Video id cannot be null")

        collectionService.getOwnedCollectionOrThrow(collectionId)

        collectionService.update(
            id = CollectionId(collectionId),
            updateCommand = RemoveVideoFromCollectionCommand(AssetId(videoId))
        )

        analyticsEventService.saveRemoveFromCollectionEvent(
            collectionId = CollectionId(collectionId),
            videoId = AssetId(videoId)
        )
    }

}