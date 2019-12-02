package com.boclips.videos.service.testsupport

import com.boclips.videos.service.domain.model.video.ContentType
import com.boclips.videos.service.infrastructure.subject.SubjectDocument
import com.boclips.videos.service.infrastructure.video.ContentPartnerDocument
import com.boclips.videos.service.infrastructure.video.DistributionMethodDocument
import com.boclips.videos.service.infrastructure.video.PlaybackDocument
import com.boclips.videos.service.infrastructure.video.SourceDocument
import com.boclips.videos.service.infrastructure.video.TopicDocument
import com.boclips.videos.service.infrastructure.video.UserRatingDocument
import com.boclips.videos.service.infrastructure.video.UserTagDocument
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
        contentType: String? = ContentType.NEWS.name,
        keywords: List<String> = emptyList(),
        subjects: List<SubjectDocument> = emptyList(),
        releaseDate: Date = Date(),
        ingestDate: Date? = null,
        legalRestrictions: String = "legal restrictions",
        language: String? = null,
        transcript: String? = null,
        topics: List<TopicDocument>? = null,
        ageRangeMin: Int? = null,
        ageRangeMax: Int? = null,
        rating: List<UserRatingDocument> = emptyList(),
        distributionMethods: Set<DistributionMethodDocument>? = null,
        tags: List<UserTagDocument> = emptyList(),
        promoted: Boolean? = null
    ): VideoDocument {
        return VideoDocument(
            id = id,
            title = title,
            description = description,
            source = source,
            playback = playback,
            contentType = contentType,
            keywords = keywords,
            subjects = subjects,
            releaseDate = releaseDate,
            ingestDate = ingestDate,
            legalRestrictions = legalRestrictions,
            language = language,
            transcript = transcript,
            topics = topics,
            ageRangeMin = ageRangeMin,
            ageRangeMax = ageRangeMax,
            rating = rating,
            distributionMethods = distributionMethods,
            tags = tags,
            promoted = promoted
        )
    }

    fun createSourceDocument(
        contentPartner: ContentPartnerDocument = createContentPartnerDocument(),
        videoReference: String = "video-reference"
    ): SourceDocument {
        return SourceDocument(
            contentPartner = contentPartner,
            videoReference = videoReference
        )
    }

    fun createContentPartnerDocument(
        id: ObjectId = ObjectId.get(),
        name: String = "content partner name",
        lastModified: Instant? = null,
        createdAt: Instant? = null
    ): ContentPartnerDocument {
        return ContentPartnerDocument(
            id = id,
            name = name,
            lastModified = lastModified,
            createdAt = createdAt
        )
    }

    fun createPlaybackDocument(
        type: String = "KALTURA",
        id: String = "playback-id",
        entryId: String? = null,
        thumbnailUrl: List<String>? = null,
        downloadUrl: String? = null,
        lastVerified: Instant? = null,
        duration: Int? = null
    ): PlaybackDocument {
        return PlaybackDocument(
            type = type,
            id = id,
            entryId = entryId,
            thumbnailUrl = thumbnailUrl,
            downloadUrl = downloadUrl,
            lastVerified = lastVerified,
            duration = duration
        )
    }
}