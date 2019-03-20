package com.boclips.videos.service.infrastructure.analytics

import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.service.collection.CollectionUpdateCommand

interface AnalyticsEventService {
    fun saveSearchEvent(
        query: String,
        pageIndex: Int,
        pageSize: Int,
        totalResults: Long
    )

    fun saveAddToCollectionEvent(collectionId: CollectionId, videoId: AssetId)

    fun saveRemoveFromCollectionEvent(collectionId: CollectionId, videoId: AssetId)

    fun saveUpdateCollectionEvent(collectiondId: CollectionId, updateCommands :List<CollectionUpdateCommand>)

    fun savePlaybackEvent(
        videoId: AssetId,
        videoIndex: Int?,
        playerId: String,
        segmentStartSeconds: Long,
        segmentEndSeconds: Long,
        videoDurationSeconds: Long
    )
}

