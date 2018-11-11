package com.boclips.videos.service.testsupport

import com.boclips.kalturaclient.media.MediaEntry
import com.boclips.kalturaclient.media.streams.StreamUrls
import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.model.asset.VideoAsset
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.asset.VideoType
import com.boclips.videos.service.domain.model.playback.*
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
            videoId: String = "asset-id",
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
            videoAsset: VideoAsset = createVideoAsset(),
            videoPlayback: VideoPlayback = createKalturaPlayback()
    ) = Video(asset = videoAsset, playback = videoPlayback)

    fun createVideoAsset(
            title: String = "title",
            description: String = "description",
            contentProvider: String = "AP",
            videoId: String = "123",
            playbackId: PlaybackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "ref-id-1"),
            type: VideoType = VideoType.INSTRUCTIONAL_CLIPS,
            keywords: List<String> = listOf("keyword")
    ): VideoAsset {
        return VideoAsset(
                assetId = AssetId(value = videoId),
                playbackId = playbackId,
                title = title,
                description = description,
                keywords = keywords,
                releasedOn = LocalDate.parse("2018-01-01"),
                contentProvider = contentProvider,
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

    fun createMediaEntry(id: String = "1", referenceId: String = "ref-id-$id", duration: Duration = Duration.ofMinutes(1)): MediaEntry? {
        return MediaEntry.builder()
                .id(id)
                .referenceId(referenceId)
                .streams(StreamUrls("https://stream/[FORMAT]/asset-$id.mp4"))
                .thumbnailUrl("https://thumbnail/thumbnail-$id.mp4")
                .duration(duration)
                .build()
    }

    fun createKalturaPlayback(): StreamPlayback {
        return StreamPlayback(streamUrl = "kaltura-stream", thumbnailUrl = "kaltura-thumbnail", duration = Duration.ofSeconds(11))
    }

    fun createYoutubePlayback(): YoutubePlayback {
        return YoutubePlayback(youtubeId = "youtube-id", thumbnailUrl = "youtube-thumbnail", duration = Duration.ofSeconds(21))
    }

}

