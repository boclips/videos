package com.boclips.videos.service.testsupport

import com.boclips.kalturaclient.media.MediaEntry
import com.boclips.kalturaclient.media.streams.StreamUrls
import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.model.VideoId
import com.boclips.videos.service.domain.model.VideoType
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.infrastructure.event.types.Event
import com.boclips.videos.service.infrastructure.event.types.NoSearchResultsEvent
import com.boclips.videos.service.infrastructure.event.types.PlaybackEvent
import com.boclips.videos.service.infrastructure.event.types.SearchEvent
import com.boclips.videos.service.infrastructure.video.VideoEntity
import java.time.Duration
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
            playbackId: PlaybackId = PlaybackId(playbackProviderType = PlaybackProviderType.KALTURA, playbackId = "ref-id-1"),
            type: VideoType = VideoType.INSTRUCTIONAL_CLIPS,
            keywords: List<String> = listOf("keyword")
    ): Video {
        return Video(
                videoId = VideoId(videoId = videoId),
                playbackId = playbackId,
                title = title,
                description = description,
                keywords = keywords,
                releasedOn = LocalDate.parse("2018-01-01"),
                contentProvider = contentProvider,
                videoPlayback = videoPlayback,
                type = type
        )
    }

    fun createVideoEntity(typeId: Int = 1, keywords: String = "k1,k2,k3", playbackId: String = "12312413-123123-123-123", playbackProvider: String = "KALTURA"): VideoEntity {
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
                reference_id = "reference_id",
                playback_provider = playbackProvider,
                playback_id = playbackId
        )
    }

    fun createMediaEntry(id: String = "1", referenceId: String = "ref-id-$id"): MediaEntry? {
        return MediaEntry.builder()
                .id(id)
                .referenceId(referenceId)
                .streams(StreamUrls("https://stream/[FORMAT]/video-$id.mp4"))
                .thumbnailUrl("https://thumbnail/thumbnail-$id.mp4")
                .duration(Duration.ofMinutes(1))
                .build()
    }

}

