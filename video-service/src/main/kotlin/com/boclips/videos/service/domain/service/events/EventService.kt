package com.boclips.videos.service.domain.service.events

import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.collection.CollectionUpdateCommand

interface EventService {
    fun saveSearchEvent(
        query: String,
        pageIndex: Int,
        pageSize: Int,
        totalResults: Long,
        pageVideoIds: List<String>
    )

    fun saveUpdateCollectionEvent(updateCommands: List<CollectionUpdateCommand>)

    fun saveCollectionDeletedEvent(collectionId: CollectionId)

    fun savePlaybackEvent(
        videoId: VideoId,
        videoIndex: Int?,
        playerId: String,
        segmentStartSeconds: Long,
        segmentEndSeconds: Long,
        playbackDevice: String?
    )

    fun savePlayerInteractedWithEvent(
        playerId: String,
        videoId: VideoId,
        currentTime: Long,
        subtype: String,
        payload: Map<String, Any>
    )

    fun publishVideoInteractedWithEvent(videoId: VideoId, subtype: String)
}

