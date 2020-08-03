package com.boclips.videos.service.infrastructure.video.converters

import com.boclips.videos.service.domain.model.AgeRange
import com.boclips.videos.service.domain.model.video.ContentType
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoSubjects
import com.boclips.videos.service.domain.model.video.Voice
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
            additionalDescription = video.additionalDescription,
            source = SourceDocument(
                channel = ChannelDocumentConverter.toChannelDocument(video.channel),
                videoReference = video.videoReference
            ),
            playback = PlaybackConverter.toDocument(video.playback),
            contentTypes = video.types.map { it.name },
            keywords = video.keywords,
            subjects = video.subjects.items.map(SubjectDocumentConverter::toSubjectDocument),
            releaseDate = Date.from(video.releasedOn.atStartOfDay().toInstant(ZoneOffset.UTC)),
            ingestedAt = video.ingestedAt.toString(),
            legalRestrictions = video.legalRestrictions,
            language = video.voice.language?.toLanguageTag(),
            transcript = video.voice.transcript,
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
            attachments = video.attachments.map { AttachmentDocumentConverter.convert(it) },
            contentWarnings = video.contentWarnings?.map { ContentWarningDocumentConverter.toDocument(it) },
            deactivated = video.deactivated,
            activeVideoId = video.activeVideoId?.let { it.value }
        )
    }

    fun toVideo(document: VideoDocument): Video {
        return Video(
            videoId = VideoId(document.id.toHexString()),
            title = document.title,
            description = document.description,
            additionalDescription = document.additionalDescription,
            channel = ChannelDocumentConverter.toChannel(document.source.channel),
            videoReference = document.source.videoReference,
            playback = PlaybackConverter.toPlayback(document.playback),
            types = document.contentTypes.map { ContentType.valueOf(it) },
            keywords = document.keywords,
            subjects = subjectsFromVideoDocument(document),
            releasedOn = document.releaseDate.toInstant().atOffset(ZoneOffset.UTC).toLocalDate(),
            ingestedAt = document.ingestedAt?.let { ZonedDateTime.parse(it) }
                ?: ZonedDateTime.ofInstant(document.id.date.toInstant(), ZoneOffset.UTC),
            legalRestrictions = document.legalRestrictions,
            voice = Voice.UnknownVoice(
                language = document.language?.let(Locale::forLanguageTag),
                transcript = document.transcript
            ),
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
            attachments = document.attachments.map { AttachmentDocumentConverter.convert(it) },
            contentWarnings = document.contentWarnings?.map { ContentWarningDocumentConverter.toContentWarning(it) },
            deactivated = document.deactivated ?: false,
            activeVideoId = document.activeVideoId?.let { VideoId(it) }
        )
    }

    private fun subjectsFromVideoDocument(document: VideoDocument): VideoSubjects =
        VideoSubjects(
            setManually = document.subjectsWereSetManually,
            items = document.subjects.map(SubjectDocumentConverter::toSubject).toSet()
        )
}
