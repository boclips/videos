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

    fun saveUpdateCollectionEvent(collectionId: CollectionId, updateCommands :List<CollectionUpdateCommand>)

    fun saveBookmarkCollectionEvent(collectionId: CollectionId)

    fun saveUnbookmarkCollectionEvent(collectionId: CollectionId)

    fun savePlaybackEvent(
        videoId: AssetId,
        videoIndex: Int?,
        playerId: String,
        segmentStartSeconds: Long,
        segmentEndSeconds: Long,
        videoDurationSeconds: Long
    )
}

