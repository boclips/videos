package com.boclips.videos.service.testsupport

import com.boclips.events.types.AnalysedVideo
import com.boclips.events.types.AnalysedVideoKeyword
import com.boclips.events.types.AnalysedVideoTopic
import com.boclips.events.types.Captions
import com.boclips.events.types.CaptionsFormat
import com.boclips.kalturaclient.captionasset.CaptionAsset
import com.boclips.kalturaclient.captionasset.KalturaLanguage
import com.boclips.kalturaclient.media.MediaEntry
import com.boclips.kalturaclient.media.MediaEntryStatus
import com.boclips.kalturaclient.media.streams.StreamUrls
import com.boclips.videos.service.domain.model.UserId
import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.asset.LegacyVideoType
import com.boclips.videos.service.domain.model.asset.LegacySubject
import com.boclips.videos.service.domain.model.Subject
import com.boclips.videos.service.domain.model.asset.Topic
import com.boclips.videos.service.domain.model.asset.VideoAsset
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.playback.StreamPlayback
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.domain.model.playback.YoutubePlayback
import com.boclips.videos.service.presentation.collections.CollectionResource
import com.boclips.videos.service.presentation.collections.CreateCollectionRequest
import com.boclips.videos.service.presentation.subject.SubjectResource
import com.boclips.videos.service.presentation.video.CreateVideoRequest
import com.boclips.videos.service.presentation.video.VideoResource
import org.bson.types.ObjectId
import org.springframework.hateoas.Resource
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.util.Locale

object TestFactories {

    fun createVideo(
        videoAsset: VideoAsset = createVideoAsset(),
        videoPlayback: VideoPlayback = createKalturaPlayback()
    ) = Video(asset = videoAsset, playback = videoPlayback)

    fun createVideoAsset(
        videoId: String = ObjectId().toHexString(),
        title: String = "title",
        description: String = "description",
        contentPartnerId: String = "Reuters",
        contentPartnerVideoId: String = "cp-id-$videoId",
        playbackId: PlaybackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "ref-id-1"),
        type: LegacyVideoType = LegacyVideoType.INSTRUCTIONAL_CLIPS,
        keywords: List<String> = listOf("keyword"),
        subjects: Set<LegacySubject> = emptySet(),
        releasedOn: LocalDate = LocalDate.parse("2018-01-01"),
        duration: Duration = Duration.ZERO,
        legalRestrictions: String = "",
        language: Locale? = null,
        transcript: String? = null,
        topics: Set<Topic> = emptySet(),
        searchable: Boolean = true
    ): VideoAsset {
        return VideoAsset(
            assetId = AssetId(value = ObjectId(videoId).toHexString()),
            playbackId = playbackId,
            title = title,
            description = description,
            keywords = keywords,
            releasedOn = releasedOn,
            contentPartnerId = contentPartnerId,
            contentPartnerVideoId = contentPartnerVideoId,
            type = type,
            duration = duration,
            legalRestrictions = legalRestrictions,
            subjects = subjects,
            language = language,
            transcript = transcript,
            topics = topics,
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
            .downloadUrl("https://download/video-$id.mp4")
            .duration(duration)
            .status(status)
            .build()
    }

    fun createKalturaPlayback(
        duration: Duration = Duration.ofSeconds(11),
        downloadUrl: String = "kaltura-download"
    ): StreamPlayback {
        val playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "555")
        return StreamPlayback(
            id = playbackId,
            streamUrl = "kaltura-stream",
            thumbnailUrl = "kaltura-thumbnail",
            downloadUrl = downloadUrl,
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
        subjects: Set<String>? = emptySet(),
        analyseVideo: Boolean = false
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
        subjects = subjects,
        analyseVideo = analyseVideo
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
        isPublic: Boolean = false,
        createdByBoclips: Boolean = false,
        bookmarks: Set<UserId> = emptySet(),
        subjects: Set<Subject> = emptySet()
    ) = Collection(
        id = id,
        owner = UserId(value = owner),
        title = title,
        videos = videos,
        updatedAt = updatedAt,
        isPublic = isPublic,
        createdByBoclips = createdByBoclips,
        bookmarks = bookmarks,
        subjects = subjects
    )

    fun createCollectionResource(
        id: String = "collection-id",
        owner: String = "collection owner",
        title: String = "collection title",
        videos: List<Resource<VideoResource>> = emptyList(),
        updatedAt: Instant = Instant.now(),
        isPublic: Boolean = false,
        isBookmarked: Boolean = false,
        isMine: Boolean = false,
        createdBy: String = "Johnny Bravo",
        subjects: Set<Resource<SubjectResource>> = emptySet()
    ) = CollectionResource(
            id = id,
            owner = owner,
            title = title,
            videos = videos,
            updatedAt = updatedAt,
            isPublic = isPublic,
            isMine = isMine,
            isBookmarked = isBookmarked,
            createdBy = createdBy,
            subjects = subjects
    )

    fun aValidId(): String {
        return ObjectId().toHexString()
    }

    fun createCaptions(
            language: Locale = Locale.US,
            autoGenerated: Boolean = true,
            format: CaptionsFormat = CaptionsFormat.VTT,
            content: String = "captions content"
    ): Captions {
        return Captions.builder()
                .language(language)
                .format(format)
                .autoGenerated(autoGenerated)
                .content(content)
                .build()
    }

    fun createAnalysedVideo(
            videoId: String = aValidId(),
            language: Locale = Locale.US,
            transcript: String = "the transcript",
            topics: List<AnalysedVideoTopic> = emptyList(),
            keywords: List<AnalysedVideoKeyword> = emptyList()
    ): AnalysedVideo {
        return AnalysedVideo
                .builder()
                .videoId(videoId)
                .language(language)
                .topics(topics)
                .keywords(keywords)
                .transcript(transcript)
                .captions(TestFactories.createCaptions())
                .build()
    }

    fun createAnalysedVideoTopic(
            name: String = "Statistics",
            language: Locale = Locale.UK,
            confidence: Double = 0.7,
            parent: AnalysedVideoTopic? = null
    ): AnalysedVideoTopic {
        return AnalysedVideoTopic.builder()
                .confidence(confidence)
                .segments(emptyList())
                .language(language)
                .parent(parent)
                .name(name)
                .build()
    }

    fun createAnalysedVideoKeyword(
            name: String = "pH"
    ): AnalysedVideoKeyword {
        return AnalysedVideoKeyword.builder()
                .name(name)
                .confidence(0.5)
                .language(Locale.US)
                .segments(emptyList())
                .build()
    }

    fun createKalturaCaptionAsset(
            language: KalturaLanguage = KalturaLanguage.ENGLISH,
            label: String = language.getName()
    ): CaptionAsset {
        return CaptionAsset.builder()
                .language(language)
                .label(label)
                .build()
    }
}

