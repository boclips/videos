package com.boclips.videos.service.testsupport

import com.boclips.kalturaclient.media.MediaEntry
import com.boclips.kalturaclient.media.streams.StreamUrls
import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.asset.VideoAsset
import com.boclips.videos.service.domain.model.asset.VideoType
import com.boclips.videos.service.domain.model.playback.*
import com.boclips.videos.service.infrastructure.event.analysis.Interaction
import com.boclips.videos.service.infrastructure.event.types.*
import com.boclips.videos.service.infrastructure.video.VideoEntity
import com.boclips.videos.service.presentation.video.CreateVideoRequest
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
            user = User.anonymous(),
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
            searchId = searchId,
            user = User.anonymous()
    )

    fun createNoSearchResultsEvent(
            captureTime: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC)
    ): Event<*> {
        return NoSearchResultsEvent(
                name = "name",
                query = "query",
                email = "email",
                description = "description",
                captureTime = captureTime,
                user = User.anonymous()
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
                contentPartnerId = contentProvider,
                contentPartnerVideoId = "cp-id-$videoId",
                type = type,
                duration = Duration.ZERO,
                legalRestrictions = ""
        )
    }

    fun createVideoEntity(typeId: Int = 1, keywords: String = "k1,k2,k3", playbackId: String = "12312413-123123-123-123", playbackProvider: String = "KALTURA", duration: String = "00:00:00", restrictions: String? = null, uniqueId: String = "unique-id"): VideoEntity {
        return VideoEntity(
                id = 123,
                source = "source",
                namespace = "namespace",
                title = "title",
                description = "description",
                date = "2014-08-13",
                duration = duration,
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
                restrictions = restrictions,
                type_id = typeId,
                reference_id = "reference_id",
                playback_provider = playbackProvider,
                playback_id = playbackId,
                unique_id = uniqueId
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
        val playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "555")
        return StreamPlayback(playbackId = playbackId,
                streamUrl = "kaltura-stream",
                thumbnailUrl = "kaltura-thumbnail",
                duration = Duration.ofSeconds(11))
    }

    fun createYoutubePlayback(): YoutubePlayback {
        val playbackId = PlaybackId(type = PlaybackProviderType.YOUTUBE, value = "444")
        return YoutubePlayback(
                playbackId = playbackId,
                youtubeId = "youtube-id",
                thumbnailUrl = "youtube-thumbnail",
                duration = Duration.ofSeconds(21))
    }

    fun createInteraction(
            timestamp: ZonedDateTime = ZonedDateTime.now(),
            description: String = "something cool",
            related: List<Interaction> = emptyList(),
            user: User = User.anonymous()
    ) = Interaction(
            timestamp = timestamp,
            description = description,
            related = related,
            user = user
    )

    fun createCreateVideoRequest(
            provider: String? = "AP",
            providerVideoId: String? = "AP-1",
            title: String? = "an AP video",
            description: String? = "an AP video about penguins",
            releasedOn: LocalDate? = LocalDate.now(),
            duration: Duration? = Duration.ofSeconds(12),
            legalRestrictions: String? = "None",
            keywords: List<String>? = listOf("k1", "k2"),
            contentType: String? = "NEWS",
            playbackId: String? = "123",
            playbackProvider: String? = "KALTURA"
    ) = CreateVideoRequest(
            provider = provider,
            providerVideoId = providerVideoId,
            title = title,
            description = description,
            releasedOn = releasedOn,
            duration = duration,
            legalRestrictions = legalRestrictions,
            keywords = keywords,
            contentType = contentType,
            playbackId = playbackId,
            playbackProvider = playbackProvider
    )

}

