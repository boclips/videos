package com.boclips.videos.service.domain.service

import com.boclips.eventbus.domain.SubjectId
import com.boclips.eventbus.domain.collection.CollectionId
import com.boclips.eventbus.domain.user.UserId
import com.boclips.eventbus.domain.video.PlaybackProviderType
import com.boclips.eventbus.domain.video.VideoId
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.common.AgeRange
import com.boclips.videos.service.domain.model.subject.Subject
import com.boclips.videos.service.domain.model.video.ContentPartner
import com.boclips.videos.service.domain.model.video.Video
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.Date

class EventConverter {
    fun toVideoPayload(video: Video): com.boclips.eventbus.domain.video.Video {
        val subjects = toSubjectPayload(video.subjects)
        return com.boclips.eventbus.domain.video.Video.builder()
            .id(com.boclips.eventbus.domain.video.VideoId(video.videoId.value))
            .title(video.title)
            .contentPartner(toContentPartnerPayload(video.contentPartner))
            .playbackProviderType(PlaybackProviderType.valueOf(video.playback.id.type.name))
            .subjects(subjects)
            .ageRange(toAgeRangePayload(video.ageRange))
            .durationSeconds(video.playback.duration.seconds.toInt())
            .build()
    }

    private fun toAgeRangePayload(ageRange: AgeRange): com.boclips.eventbus.domain.AgeRange {
        return com.boclips.eventbus.domain.AgeRange.builder()
            .min(ageRange.min())
            .max(ageRange.max())
            .build()
    }

    private fun toSubjectPayload(subjects: Set<Subject>): List<com.boclips.eventbus.domain.Subject> {
        return subjects.map {
            com.boclips.eventbus.domain.Subject.builder()
                .id(SubjectId(it.id.value))
                .name(it.name)
                .build()
        }
    }

    private fun toContentPartnerPayload(contentPartner: ContentPartner):
        com.boclips.eventbus.domain.video.ContentPartner {
        return com.boclips.eventbus.domain.video.ContentPartner.of(contentPartner.name)
    }


    fun toCollectionPayload(collection: Collection): com.boclips.eventbus.domain.collection.Collection {
        return com.boclips.eventbus.domain.collection.Collection.builder()
            .id(CollectionId(collection.id.value))
            .updatedTime(Date.from(ZonedDateTime.now(ZoneOffset.UTC).toInstant()))
            .title(collection.title)
            .description(collection.description ?: "")
            .subjects(toSubjectPayload(collection.subjects))
            .videosIds(collection.videos.map { VideoId(it.value) })
            .ownerId(UserId(collection.owner.value))
            .visible(collection.isPublic)
            .ageRange(com.boclips.eventbus.domain.AgeRange(collection.ageRange.min(), collection.ageRange.max()))
            .bookmarks(collection.bookmarks.map{ UserId(it.value)})
            .build()
    }
}
