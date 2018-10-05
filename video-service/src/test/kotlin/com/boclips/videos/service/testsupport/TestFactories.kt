package com.boclips.videos.service.testsupport

import com.boclips.videos.service.application.event.PlaybackEvent
import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.model.VideoId
import com.boclips.videos.service.domain.model.VideoPlayback
import com.boclips.videos.service.infrastructure.event.SearchEvent
import com.boclips.videos.service.infrastructure.search.ElasticSearchVideo
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZonedDateTime

object TestFactories {

    fun createElasticSearchVideos(
            id: String = "video-id",
            referenceId: String = "ref-id",
            title: String = "video-title",
            source: String = "video-source",
            date: String = "2018-01-02",
            description: String = "video-description"
    ) = ElasticSearchVideo(
            id = id,
            referenceId = referenceId,
            title = title,
            source = source,
            date = date,
            description = description
    )

    fun createSearchEvent(
            timestamp: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),
            searchId: String = "search-id",
            query: String = "muscles",
            resultsReturned: Int = 10
    ) = SearchEvent(
            timestamp = timestamp,
            correlationId = searchId,
            query = query,
            resultsReturned = resultsReturned
    )

    fun createPlaybackEvent(
            playerId: String = "player-id",
            videoId: String = "video-id",
            segmentStartSeconds: Long = 0,
            segmentEndSeconds: Long = 30,
            videoDurationSeconds: Long = 60,
            captureTime: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),
            searchId: String? = null
    ) = PlaybackEvent(
            playerId = playerId,
            videoId = videoId,
            segmentStartSeconds = segmentStartSeconds,
            segmentEndSeconds = segmentEndSeconds,
            videoDurationSeconds = videoDurationSeconds,
            captureTime = captureTime,
            searchId = searchId
    )

    fun createVideo(videoPlayback: VideoPlayback? = null, videoId: String = "123", referenceId: String? = "ref-id-1"): Video {
        return Video(
                videoId = VideoId(videoId = videoId, referenceId = referenceId),
                title = "title",
                description = "description",
                releasedOn = LocalDate.parse("2018-01-01"),
                videoPlayback = videoPlayback,
                contentProvider = "AP"
        )
    }

}

