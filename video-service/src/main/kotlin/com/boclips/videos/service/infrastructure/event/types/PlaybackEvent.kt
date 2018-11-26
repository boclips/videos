package com.boclips.videos.service.infrastructure.event.types

import java.time.ZonedDateTime

data class PlaybackEventData(
        val playerId: String,
        val videoId: String,
        val segmentStartSeconds: Long,
        val segmentEndSeconds: Long,
        val videoDurationSeconds: Long,
        val searchId: String?
)

class PlaybackEvent(
        playerId: String,
        videoId: String,
        user: User,
        segmentStartSeconds: Long,
        segmentEndSeconds: Long,
        videoDurationSeconds: Long,
        captureTime: ZonedDateTime,
        searchId: String?
) : Event<PlaybackEventData>(EventType.PLAYBACK.name, captureTime, user, PlaybackEventData(
        playerId = playerId,
        videoId = videoId,
        segmentStartSeconds = segmentStartSeconds,
        segmentEndSeconds = segmentEndSeconds,
        videoDurationSeconds = videoDurationSeconds,
        searchId = searchId
))