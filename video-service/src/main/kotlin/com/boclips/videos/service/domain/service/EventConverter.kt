package com.boclips.videos.service.domain.service

import com.boclips.eventbus.domain.SubjectId
import com.boclips.eventbus.domain.video.PlaybackProviderType
import com.boclips.videos.service.domain.model.common.AgeRange
import com.boclips.videos.service.domain.model.subject.Subject
import com.boclips.videos.service.domain.model.video.ContentPartner
import com.boclips.videos.service.domain.model.video.Video

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
}
