package com.boclips.videos.service.domain.service

import com.boclips.eventbus.domain.SubjectId
import com.boclips.eventbus.domain.collection.CollectionId
import com.boclips.eventbus.domain.user.UserId
import com.boclips.eventbus.domain.video.PlaybackProviderType
import com.boclips.eventbus.domain.video.VideoId
import com.boclips.eventbus.domain.video.VideoType
import com.boclips.videos.service.domain.model.AgeRange
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.subject.Subject
import com.boclips.videos.service.domain.model.video.ContentPartner
import com.boclips.videos.service.domain.model.video.ContentType
import com.boclips.videos.service.domain.model.video.Video

class EventConverter {
    fun toVideoPayload(video: Video): com.boclips.eventbus.domain.video.Video {
        val subjects = toSubjectPayload(video.subjects)
        return com.boclips.eventbus.domain.video.Video.builder()
            .id(VideoId(video.videoId.value))
            .title(video.title)
            .contentPartner(toContentPartnerPayload(video.contentPartner))
            .playbackProviderType(PlaybackProviderType.valueOf(video.playback.id.type.name))
            .subjects(subjects)
            .ageRange(toAgeRangePayload(video.ageRange))
            .durationSeconds(video.playback.duration.seconds.toInt())
            .type(toVideoType(video.type))
            .ingestedOn(video.ingestedOn)
            .build()
    }

    private fun toVideoType(contentType: ContentType): VideoType {
        return when (contentType) {
            ContentType.INSTRUCTIONAL_CLIPS -> VideoType.INSTRUCTIONAL
            ContentType.NEWS -> VideoType.NEWS
            ContentType.STOCK -> VideoType.STOCK
        }
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
            .createdAt(collection.createdAt)
            .updatedAt(collection.updatedAt)
            .title(collection.title)
            .description(collection.description ?: "")
            .subjects(toSubjectPayload(collection.subjects))
            .videosIds(collection.videos.map { VideoId(it.value) })
            .ownerId(UserId(collection.owner.value))
            .isPublic(collection.isPublic)
            .ageRange(com.boclips.eventbus.domain.AgeRange(collection.ageRange.min(), collection.ageRange.max()))
            .bookmarks(collection.bookmarks.map { UserId(it.value) })
            .build()
    }
}
