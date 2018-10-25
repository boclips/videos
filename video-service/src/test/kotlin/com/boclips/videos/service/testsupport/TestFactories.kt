package com.boclips.videos.service.testsupport

import com.boclips.videos.service.infrastructure.event.types.PlaybackEvent
import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.model.VideoId
import com.boclips.videos.service.domain.model.VideoPlayback
import com.boclips.videos.service.domain.model.VideoType
import com.boclips.videos.service.infrastructure.event.types.Event
import com.boclips.videos.service.infrastructure.event.types.NoSearchResultsEvent
import com.boclips.videos.service.infrastructure.event.types.SearchEvent
import com.boclips.videos.service.infrastructure.video.VideoEntity
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZonedDateTime

object TestFactories {

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

    fun createNoSearchResultsEvent(
            captureTime: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC)
    ): Event<*> {
        return NoSearchResultsEvent(
                name = "name",
                query = "query",
                email = "email",
                description = "description",
                captureTime = captureTime
        )
    }

    fun createVideo(
            title: String = "title",
            description: String = "description",
            contentProvider: String = "AP",
            videoPlayback: VideoPlayback? = null,
            videoId: String = "123",
            referenceId: String? = "ref-id-1",
            type: VideoType = VideoType.INSTRUCTIONAL_CLIPS,
            keywords: List<String> = listOf("keyword")
    ): Video {
        return Video(
                videoId = VideoId(videoId = videoId, referenceId = referenceId),
                title = title,
                description = description,
                releasedOn = LocalDate.parse("2018-01-01"),
                videoPlayback = videoPlayback,
                contentProvider = contentProvider,
                type = type,
                keywords = keywords
        )
    }

    fun createVideoEntity(typeId: Int = 1, keywords: String = "k1,k2,k3"): VideoEntity {
        return VideoEntity(
                id = 123,
                source = "source",
                namespace = "namespace",
                title = "title",
                description = "description",
                date = "2014-08-13",
                duration = "duration",
                keywords = keywords,
                price_category = "price_category",
                sounds = "sounds",
                color = "color",
                location = "location",
                country = "country",
                state = "state",
                city = "city",
                region = "region",
                alternative_id = "alternative_id",
                alt_source = "alt_source",
                restrictions = "restrictions",
                type_id = typeId,
                reference_id = "reference_id"
        )
    }

}

