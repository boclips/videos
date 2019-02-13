package com.boclips.videos.service.domain.model.asset

import com.boclips.videos.service.domain.model.playback.PlaybackId
import java.time.Duration
import java.time.LocalDate

data class PartialVideoAsset(
    val playbackId: PlaybackId? = null,
    val title: String? = null,
    val description: String? = null,
    val keywords: List<String>? = null,
    val releasedOn: LocalDate? = null,
    val contentPartnerId: String? = null,
    val contentPartnerVideoId: String? = null,
    val type: LegacyVideoType? = null,
    val duration: Duration? = null,
    val legalRestrictions: String? = null,
    val subjects: Set<Subject>? = null,
    val searchable: Boolean? = null
)
