package com.boclips.videos.service.domain.model

import com.boclips.videos.service.domain.model.playback.FaultyPlayback
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.domain.model.video.LegacySubject
import com.boclips.videos.service.domain.model.video.LegacyVideoType
import com.boclips.videos.service.domain.model.video.Topic
import com.boclips.videos.service.domain.model.video.VideoId
import java.time.LocalDate
import java.util.Locale

data class Video(
    val videoId: VideoId,
    val playback: VideoPlayback,
    val title: String,
    val description: String,
    val keywords: List<String>,
    val releasedOn: LocalDate,
    val contentPartnerId: String,
    val contentPartnerVideoId: String,
    val type: LegacyVideoType,
    val legalRestrictions: String,
    val subjects: Set<LegacySubject>,
    val topics: Set<Topic>,
    val language: Locale?,
    val transcript: String?,
    val searchable: Boolean
) {
    fun isPlayable(): Boolean {
        return playback !is FaultyPlayback
    }

    fun isBoclipsHosted(): Boolean {
        return playback.id.type == PlaybackProviderType.KALTURA
    }

    override fun toString(): String {
        return "Video(videoId=$videoId, title='$title', contentPartnerId='$contentPartnerId')"
    }
}
