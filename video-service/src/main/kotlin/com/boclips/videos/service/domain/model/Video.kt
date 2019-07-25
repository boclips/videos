package com.boclips.videos.service.domain.model

import com.boclips.eventbus.domain.video.Video
import com.boclips.videos.service.application.getCurrentUserId
import com.boclips.videos.service.domain.model.common.AgeRange
import com.boclips.videos.service.domain.model.contentPartner.ContentPartner
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.domain.model.subject.Subject
import com.boclips.videos.service.domain.model.tag.UserTag
import com.boclips.videos.service.domain.model.video.*
import java.time.LocalDate
import java.util.*

data class Video(
    val videoId: VideoId,
    val playback: VideoPlayback,
    val contentPartner: ContentPartner,
    val videoReference: String,
    val title: String,
    val description: String,
    val keywords: List<String>,
    val releasedOn: LocalDate,
    val type: LegacyVideoType,
    val legalRestrictions: String,
    val subjects: Set<Subject>,
    val eventBus: Set<Topic>,
    val language: Locale?,
    val transcript: String?,
    val ageRange: AgeRange,
    val ratings: List<UserRating>,
    val tag: UserTag?,
    val distributionMethods: Set<DistributionMethod>
) {
    fun isPlayable(): Boolean {
        return playback !is VideoPlayback.FaultyPlayback
    }

    fun isBoclipsHosted(): Boolean {
        return playback is VideoPlayback.StreamPlayback
    }

    override fun toString(): String {
        return "Video(videoId=$videoId, title='$title', contentPartnerName='${contentPartner.name}')"
    }

    fun getRatingAverage() = when {
        this.ratings.isEmpty() -> null
        else -> this.ratings.map { it.rating }.average()
    }

    fun isRatedByCurrentUser() =
        ratings.any { it.userId == getCurrentUserId() }

    fun toEvent(): Video {
        val subjects = subjects.map(Subject::toEvent)

        return Video.builder()
            .id(videoId.toEvent())
            .title(title)
            .contentPartner(contentPartner.toEvent())
            .subjects(subjects)
            .ageRange(ageRange.toEvent())
            .build()
    }


}
