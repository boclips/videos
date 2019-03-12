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
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.playback.StreamPlayback
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.domain.model.playback.YoutubePlayback
import com.boclips.videos.service.presentation.collections.CreateCollectionRequest
import com.boclips.videos.service.presentation.video.CreateVideoRequest
import org.bson.types.ObjectId
import java.time.Duration
import java.time.Instant
import java.time.LocalDate

object TestFactories {

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

    fun createKalturaPlayback(duration: Duration = Duration.ofSeconds(11)): StreamPlayback {
        val playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "555")
        return StreamPlayback(
            id = playbackId,
            streamUrl = "kaltura-stream",
            thumbnailUrl = "kaltura-thumbnail",
            duration = duration
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
        legalRestrictions = legalRestrictions,
        keywords = keywords,
        videoType = contentType,
        playbackId = playbackId,
        playbackProvider = playbackProvider,
        subjects = subjects
    )

    fun createCollectionRequest(
        title: String? = "collection title",
        videos: List<String> = listOf()
    ) = CreateCollectionRequest(
        title = title,
        videos = videos
    )

    fun createCollection(
        id: CollectionId = CollectionId("collection-id"),
        owner: String = "collection owner",
        title: String = "collection title",
        videos: List<AssetId> = listOf(createVideo().asset.assetId),
        updatedAt: Instant = Instant.now(),
        isPublic: Boolean = false
    ) = Collection(
        id = id,
        owner = UserId(value = owner),
        title = title,
        videos = videos,
        updatedAt = updatedAt,
        isPublic = isPublic
    )

    fun aValidId(): String {
        return ObjectId().toHexString()
    }
}

