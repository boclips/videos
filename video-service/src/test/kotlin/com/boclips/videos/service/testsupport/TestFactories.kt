package com.boclips.videos.service.testsupport

import com.boclips.kalturaclient.media.MediaEntry
import com.boclips.kalturaclient.media.MediaEntryStatus
import com.boclips.kalturaclient.media.streams.StreamUrls
import com.boclips.videos.service.domain.model.UserId
import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.asset.LegacyVideoType
import com.boclips.videos.service.domain.model.asset.Subject
import com.boclips.videos.service.domain.model.asset.VideoAsset
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.playback.*
import com.boclips.videos.service.infrastructure.event.types.*
import com.boclips.videos.service.presentation.video.CreateVideoRequest
import org.bson.types.ObjectId
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZonedDateTime

object TestFactories {

    fun createSearchEvent(
        timestamp: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),
        query: String = "muscles",
        resultsReturned: Int = 10
    ) = SearchEvent(
        timestamp = timestamp,
        user = User.anonymous(),
        query = query,
        page = 1,
        resultsReturned = resultsReturned
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
        videoId: String = ObjectId().toHexString(),
        title: String = "title",
        description: String = "description",
        contentProvider: String = "AP",
        contentPartnerVideoId: String = "cp-id-$videoId",
        playbackId: PlaybackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "ref-id-1"),
        type: LegacyVideoType = LegacyVideoType.INSTRUCTIONAL_CLIPS,
        keywords: List<String> = listOf("keyword"),
        subjects: Set<Subject> = emptySet(),
        releasedOn: LocalDate = LocalDate.parse("2018-01-01"),
        duration: Duration = Duration.ZERO,
        legalRestrictions: String = "",
        searchable: Boolean = true
    ): VideoAsset {
        return VideoAsset(
            assetId = AssetId(value = ObjectId(videoId).toHexString()),
            playbackId = playbackId,
            title = title,
            description = description,
            keywords = keywords,
            releasedOn = releasedOn,
            contentPartnerId = contentProvider,
            contentPartnerVideoId = contentPartnerVideoId,
            type = type,
            duration = duration,
            legalRestrictions = legalRestrictions,
            subjects = subjects,
            searchable = searchable
        )
    }

    fun createMediaEntry(
        id: String = "1",
        referenceId: String = "ref-id-$id",
        duration: Duration = Duration.ofMinutes(1),
        status: MediaEntryStatus = MediaEntryStatus.READY
    ): MediaEntry? {
        return MediaEntry.builder()
            .id(id)
            .referenceId(referenceId)
            .streams(StreamUrls("https://stream/[FORMAT]/asset-$id.mp4"))
            .thumbnailUrl("https://thumbnail/thumbnail-$id.mp4")
            .duration(duration)
            .status(status)
            .build()
    }

    fun createKalturaPlayback(): StreamPlayback {
        val playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "555")
        return StreamPlayback(
            id = playbackId,
            streamUrl = "kaltura-stream",
            thumbnailUrl = "kaltura-thumbnail",
            duration = Duration.ofSeconds(11)
        )
    }

    fun createYoutubePlayback(): YoutubePlayback {
        val playbackId = PlaybackId(type = PlaybackProviderType.YOUTUBE, value = "444")
        return YoutubePlayback(
            id = playbackId,
            thumbnailUrl = "youtube-thumbnail",
            duration = Duration.ofSeconds(21)
        )
    }

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
        playbackProvider: String? = "KALTURA",
        subjects: Set<String>? = emptySet()
    ) = CreateVideoRequest(
        provider = provider,
        providerVideoId = providerVideoId,
        title = title,
        description = description,
        releasedOn = releasedOn,
        duration = duration,
        legalRestrictions = legalRestrictions,
        keywords = keywords,
        videoType = contentType,
        playbackId = playbackId,
        playbackProvider = playbackProvider,
        subjects = subjects
    )

    fun createCollection(
        id: CollectionId = CollectionId("collection-id"),
        owner: String = "collection owner",
        title: String = "collection title",
        videos: List<Video> = listOf(createVideo())
    ) = Collection(
        id = id,
        owner = UserId(value = owner),
        title = title,
        videos = videos
    )

    fun aValidId(): String {
        return ObjectId().toHexString()
    }
}

