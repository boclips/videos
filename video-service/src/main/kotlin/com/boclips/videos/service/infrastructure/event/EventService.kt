package com.boclips.videos.service.infrastructure.event

import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.collection.CollectionId

interface EventService {
    fun saveSearchEvent(
        query: String,
        pageIndex: Int,
        pageSize: Int,
        totalResults: Long
    )

    fun saveAddToCollectionEvent(collectionId: CollectionId, videoId: AssetId)

    fun saveRemoveFromCollectionEvent(collectionId: CollectionId, videoId: AssetId)

    fun savePlaybackEvent(
        videoId: AssetId,
        videoIndex: Int?,
        playerId: String,
        segmentStartSeconds: Long,
        segmentEndSeconds: Long,
        videoDurationSeconds: Long
    )
}

