package com.boclips.videos.service.domain.model

import com.boclips.videos.service.domain.model.ageRange.AgeRange
import com.boclips.videos.service.domain.model.subjects.Subject
import com.boclips.videos.service.domain.model.contentPartner.ContentPartner
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.domain.model.video.LegacyVideoType
import com.boclips.videos.service.domain.model.video.Topic
import com.boclips.videos.service.domain.model.video.VideoId
import java.time.LocalDate
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
    val type: LegacyVideoType,
    val legalRestrictions: String,
    val subjects: Set<Subject>,
    val topics: Set<Topic>,
    val language: Locale?,
    val transcript: String?,
    val searchable: Boolean,
    val ageRange: AgeRange
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Video

        if (videoId != other.videoId) return false

        return true
    }

    override fun hashCode(): Int {
        return videoId.hashCode()
    }
}
