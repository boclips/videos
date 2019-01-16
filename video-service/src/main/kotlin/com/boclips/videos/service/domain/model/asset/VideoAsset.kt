package com.boclips.videos.service.domain.model.asset

import com.boclips.videos.service.domain.model.playback.PlaybackId
import java.time.Duration
import java.time.LocalDate


data class VideoAsset(
        val assetId: AssetId,
        val playbackId: PlaybackId,
        val title: String,
        val description: String,
        val keywords: List<String>,
        val releasedOn: LocalDate,
        val contentPartnerId: String,
        val contentPartnerVideoId: String,
        val type: LegacyVideoType,
        val duration: Duration,
        val legalRestrictions: String,
        val subjects: Set<Subject>
)