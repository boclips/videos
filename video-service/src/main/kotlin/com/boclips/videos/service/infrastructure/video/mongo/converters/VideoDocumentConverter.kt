package com.boclips.videos.service.infrastructure.video.mongo.converters

import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.model.ageRange.AgeRange
import com.boclips.videos.service.domain.model.video.LegacyVideoType
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.infrastructure.contentPartner.ContentPartnerDocument
import com.boclips.videos.service.infrastructure.contentPartner.ContentPartnerDocumentConverter
import com.boclips.videos.service.infrastructure.subject.mongo.converters.SubjectDocumentConverter
import com.boclips.videos.service.infrastructure.video.mongo.LegacyDocument
import com.boclips.videos.service.infrastructure.video.mongo.SourceDocument
import com.boclips.videos.service.infrastructure.video.mongo.VideoDocument
import org.bson.types.ObjectId
import java.time.ZoneOffset
import java.util.Date
import java.util.Locale

object VideoDocumentConverter {
    fun toVideoDocument(video: Video): VideoDocument {
        return VideoDocument(
            id = ObjectId(video.videoId.value),
            title = video.title,
            description = video.description,
            source = SourceDocument(
                contentPartner = ContentPartnerDocument(
                    id = ObjectId(video.contentPartner.contentPartnerId.value),
                    name = video.contentPartner.name,
                    ageRangeMin = video.contentPartner.ageRange.min(),
                    ageRangeMax = video.contentPartner.ageRange.max()
                ),
                videoReference = video.videoReference
            ),
            playback = PlaybackConverter.toDocument(video.playback),
            legacy = LegacyDocument(type = video.type.name),
            keywords = video.keywords,
            subjects = video.subjects.map(SubjectDocumentConverter::toSubjectDocument),
            releaseDate = Date.from(video.releasedOn.atStartOfDay().toInstant(ZoneOffset.UTC)),
            legalRestrictions = video.legalRestrictions,
            language = video.language?.toLanguageTag(),
            transcript = video.transcript,
            topics = video.topics.map(TopicDocumentConverter::toDocument),
            searchable = video.searchable,
            ageRangeMin = video.ageRange.min(),
            ageRangeMax = video.ageRange.max()
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
            type = LegacyVideoType.valueOf(document.legacy.type),
            keywords = document.keywords,
            subjects = document.subjects.map(SubjectDocumentConverter::toSubject).toSet(),
            releasedOn = document.releaseDate.toInstant().atOffset(ZoneOffset.UTC).toLocalDate(),
            legalRestrictions = document.legalRestrictions,
            language = document.language?.let(Locale::forLanguageTag),
            transcript = document.transcript,
            topics = document.topics.orEmpty().map(TopicDocumentConverter::toTopic).toSet(),
            searchable = document.searchable,
            ageRange = if (document.ageRangeMin !== null) AgeRange.bounded(
                min = document.ageRangeMin,
                max = document.ageRangeMax
            ) else AgeRange.unbounded()
        )
    }
}
