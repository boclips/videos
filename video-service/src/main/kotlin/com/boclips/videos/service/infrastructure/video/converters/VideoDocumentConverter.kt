package com.boclips.videos.service.infrastructure.video.converters

import com.boclips.videos.service.domain.model.AgeRange
import com.boclips.videos.service.domain.model.video.ContentType
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoSubjects
import com.boclips.videos.service.infrastructure.attachment.AttachmentDocumentConverter
import com.boclips.videos.service.infrastructure.subject.SubjectDocumentConverter
import com.boclips.videos.service.infrastructure.video.SourceDocument
import com.boclips.videos.service.infrastructure.video.VideoDocument
import org.bson.types.ObjectId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.Date
import java.util.Locale

object VideoDocumentConverter {
    fun toVideoDocument(video: Video): VideoDocument {
        return VideoDocument(
            id = ObjectId(video.videoId.value),
            title = video.title,
            description = video.description,
            source = SourceDocument(
                contentPartner = ContentPartnerDocumentConverter.toContentPartnerDocument(video.contentPartner),
                videoReference = video.videoReference
            ),
            playback = PlaybackConverter.toDocument(video.playback),
            contentType = video.type.name,
            keywords = video.keywords,
            subjects = video.subjects.items.map(SubjectDocumentConverter::toSubjectDocument),
            releaseDate = Date.from(video.releasedOn.atStartOfDay().toInstant(ZoneOffset.UTC)),
            ingestedAt = video.ingestedAt.toString(),
            legalRestrictions = video.legalRestrictions,
            language = video.language?.toLanguageTag(),
            transcript = video.transcript,
            topics = video.topics.map(TopicDocumentConverter::toDocument),
            ageRangeMin = video.ageRange.min(),
            ageRangeMax = video.ageRange.max(),
            ageRangeSetManually = video.ageRange.curatedManually,
            rating = video.ratings.map {
                UserRatingDocumentConverter.toDocument(
                    it
                )
            },
            tags = video.tags.map {
                UserTagDocumentConverter.toDocument(
                    it
                )
            },
            promoted = video.promoted,
            subjectsWereSetManually = video.subjects.setManually,
            attachments = video.attachments.map { AttachmentDocumentConverter.convert(it) }
        )
    }

    fun toVideo(document: VideoDocument): Video {
        return Video(
            videoId = VideoId(document.id.toHexString()),
            title = document.title,
            description = document.description,
            contentPartner = ContentPartnerDocumentConverter.toContentPartner(document.source.contentPartner),
            videoReference = document.source.videoReference,
            playback = PlaybackConverter.toPlayback(document.playback),
            type = ContentType.valueOf(document.contentType!!),
            keywords = document.keywords,
            subjects = subjectsFromVideoDocument(document),
            releasedOn = document.releaseDate.toInstant().atOffset(ZoneOffset.UTC).toLocalDate(),
            ingestedAt = document.ingestedAt?.let { ZonedDateTime.parse(it) }
                ?: ZonedDateTime.ofInstant(document.id.date.toInstant(), ZoneOffset.UTC),
            legalRestrictions = document.legalRestrictions,
            language = document.language?.let(Locale::forLanguageTag),
            transcript = document.transcript,
            topics = document.topics.orEmpty().map(TopicDocumentConverter::toTopic).toSet(),
            ageRange = AgeRange.of(
                min = document.ageRangeMin,
                max = document.ageRangeMax,
                curatedManually = document.ageRangeSetManually ?: false
            ),
            ratings = document.rating.map {
                UserRatingDocumentConverter.toRating(
                    it
                )
            },
            tags = document.tags.map {
                UserTagDocumentConverter.toTag(
                    it
                )
            },
            promoted = document.promoted,
            attachments = document.attachments.map { AttachmentDocumentConverter.convert(it) }
        )
    }

    private fun subjectsFromVideoDocument(document: VideoDocument): VideoSubjects =
        VideoSubjects(
            setManually = document.subjectsWereSetManually,
            items = document.subjects.map(SubjectDocumentConverter::toSubject).toSet()
        )
}
