package com.boclips.videos.service.domain.model.video

import com.boclips.videos.service.domain.model.AgeRange
import com.boclips.videos.service.domain.model.attachment.Attachment
import com.boclips.videos.service.domain.model.contentwarning.ContentWarning
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.domain.model.tag.UserTag
import com.boclips.videos.service.domain.model.user.UserId
import com.boclips.videos.service.domain.model.video.contentpartner.ContentPartner
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
    val types: List<ContentType>,
    val legalRestrictions: String,
    val subjects: VideoSubjects,
    val topics: Set<Topic>,
    val language: Locale?,
    val transcript: String?,
    val ageRange: AgeRange,
    val ratings: List<UserRating>,
    val tags: List<UserTag>,
    val promoted: Boolean?,
    val attachments: List<Attachment>,
    val contentWarnings: List<ContentWarning>?,
    val deactivated: Boolean,
    val activeVideoId: VideoId?
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

    fun hasTranscript(): Boolean {
        return this.transcript != null
    }

    override fun toString(): String {
        return "Video(videoId=$videoId, title='$title', contentPartnerName='${contentPartner.name}', videoReference='$videoReference')"
    }
}
