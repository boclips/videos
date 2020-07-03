package com.boclips.videos.service.testsupport

import com.boclips.videos.service.domain.model.playback.Dimensions
import com.boclips.videos.service.domain.model.video.ContentType
import com.boclips.videos.service.domain.model.video.VideoAsset
import com.boclips.videos.service.infrastructure.subject.SubjectDocument
import com.boclips.videos.service.infrastructure.video.ChannelDocument
import com.boclips.videos.service.infrastructure.video.PlaybackDocument
import com.boclips.videos.service.infrastructure.video.SourceDocument
import com.boclips.videos.service.infrastructure.video.TopicDocument
import com.boclips.videos.service.infrastructure.video.UserRatingDocument
import com.boclips.videos.service.infrastructure.video.UserTagDocument
import com.boclips.videos.service.infrastructure.video.VideoAssetDocument
import com.boclips.videos.service.infrastructure.video.VideoDocument
import org.bson.types.ObjectId
import java.time.Instant
import java.util.Date

object VideoFactory {

    fun createVideoDocument(
        id: ObjectId = ObjectId.get(),
        title: String = "title",
        description: String = "description",
        source: SourceDocument = createSourceDocument(),
        playback: PlaybackDocument? = createPlaybackDocument(),
        contentTypes: List<String> = listOf(ContentType.NEWS.name),
        keywords: List<String> = emptyList(),
        subjects: List<SubjectDocument> = emptyList(),
        releaseDate: Date = Date(),
        ingestedAt: String? = null,
        legalRestrictions: String = "legal restrictions",
        language: String? = null,
        transcript: String? = null,
        topics: List<TopicDocument>? = null,
        ageRangeMin: Int? = null,
        ageRangeMax: Int? = null,
        rating: List<UserRatingDocument> = emptyList(),
        tags: List<UserTagDocument> = emptyList(),
        promoted: Boolean? = null,
        subjectsWereSetManually: Boolean? = false
    ): VideoDocument {
        return VideoDocument(
            id = id,
            title = title,
            description = description,
            source = source,
            playback = playback,
            contentTypes = contentTypes,
            keywords = keywords,
            subjects = subjects,
            releaseDate = releaseDate,
            ingestedAt = ingestedAt,
            legalRestrictions = legalRestrictions,
            language = language,
            transcript = transcript,
            topics = topics,
            ageRangeMin = ageRangeMin,
            ageRangeMax = ageRangeMax,
            rating = rating,
            tags = tags,
            promoted = promoted,
            subjectsWereSetManually = subjectsWereSetManually
        )
    }

    fun createSourceDocument(
        channel: ChannelDocument = createContentPartnerDocument(),
        videoReference: String = "video-reference"
    ): SourceDocument {
        return SourceDocument(
            channel = channel,
            videoReference = videoReference
        )
    }

    fun createContentPartnerDocument(
        id: ObjectId = ObjectId.get(),
        name: String = "content partner name",
        lastModified: Instant? = null,
        createdAt: Instant? = null
    ): ChannelDocument {
        return ChannelDocument(
            id = id,
            name = name,
            lastModified = lastModified,
            createdAt = createdAt
        )
    }

    fun createVideoAsset(
        reference: String = "asset-1",
        sizeKb: Int = 1024,
        dimensions: Dimensions = Dimensions(
            width = 360,
            height = 480
        ),
        bitrateKbps: Int = 128
    ): VideoAsset {
        return VideoAsset(
            reference = reference,
            sizeKb = sizeKb,
            dimensions = dimensions,
            bitrateKbps = bitrateKbps
        )
    }

    fun createVideoAssetDocument(
        id: String? = "asset-id",
        sizeKb: Int? = 1024,
        width: Int? = 1920,
        height: Int? = 1080,
        bitrateKbps: Int? = 1024
    ): VideoAssetDocument {
        return VideoAssetDocument(
            id = id,
            sizeKb = sizeKb,
            width = width,
            height = height,
            bitrateKbps = bitrateKbps
        )
    }

    fun createPlaybackDocument(
        type: String = "KALTURA",
        id: String = "playback-id",
        entryId: String? = null,
        thumbnailUrl: List<String>? = null,
        thumbnailSecond: Int? = null,
        customThumbnail: Boolean? = null,
        downloadUrl: String? = null,
        lastVerified: Instant? = null,
        duration: Int? = null,
        assets: List<VideoAssetDocument> = emptyList(),
        originalWidth: Int? = null,
        originalHeight: Int? = null
    ): PlaybackDocument {
        return PlaybackDocument(
            type = type,
            id = id,
            entryId = entryId,
            thumbnailUrl = thumbnailUrl,
            thumbnailSecond = thumbnailSecond,
            customThumbnail = customThumbnail,
            downloadUrl = downloadUrl,
            lastVerified = lastVerified,
            duration = duration,
            assets = assets,
            originalHeight = originalHeight,
            originalWidth = originalWidth
        )
    }
}
