package com.boclips.videos.service.domain.model.video

import com.boclips.videos.service.domain.model.AgeRange
import com.boclips.videos.service.domain.model.UserId
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.domain.model.tag.UserTag
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.Locale

data class Video(
    val videoId: VideoId,
    val playback: VideoPlayback,
    val contentPartner: ContentPartner,
    val videoReference: String,
    val title: String,
    val description: String,
    val keywords: List<String>,
    val releasedOn: LocalDate,
    val ingestedAt: ZonedDateTime,
    val type: ContentType,
    val legalRestrictions: String,
    val subjects: VideoSubjects,
    val topics: Set<Topic>,
    val language: Locale?,
    val transcript: String?,
    val ageRange: AgeRange,
    val ratings: List<UserRating>,
    val tag: UserTag?,
    val promoted: Boolean?,
    val shareCodes: Set<String>?
) {
    fun isPlayable(): Boolean {
        return playback !is VideoPlayback.FaultyPlayback
    }

    fun isBoclipsHosted(): Boolean {
        return playback is VideoPlayback.StreamPlayback
    }

    fun getRatingAverage() = when {
        this.ratings.isEmpty() -> null
        else -> this.ratings.map { it.rating }.average()
    }

    fun isRatedByUser(user: UserId) =
        ratings.any { it.userId == user }

    override fun toString(): String {
        return "Video(videoId=$videoId, title='$title', contentPartnerName='${contentPartner.name}', videoReference='$videoReference')"
    }
}
