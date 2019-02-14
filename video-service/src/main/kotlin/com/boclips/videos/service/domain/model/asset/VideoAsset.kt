package com.boclips.videos.service.domain.model.asset

import com.boclips.videos.service.domain.model.playback.PlaybackId
import org.bouncycastle.asn1.x500.style.RFC4519Style.title
import java.time.Duration
import java.time.LocalDate

interface VideoAssetAttributes {
    val playbackId: PlaybackId?
    val title: String?
    val description: String?
    val keywords: List<String>?
    val releasedOn: LocalDate?
    val contentPartnerId: String?
    val contentPartnerVideoId: String?
    val type: LegacyVideoType?
    val duration: Duration?
    val legalRestrictions: String?
    val subjects: Set<Subject>?
    val searchable: Boolean?
}

data class VideoAsset(
    val assetId: AssetId,
    override val playbackId: PlaybackId,
    override val title: String,
    override val description: String,
    override val keywords: List<String>,
    override val releasedOn: LocalDate,
    override val contentPartnerId: String,
    override val contentPartnerVideoId: String,
    override val type: LegacyVideoType,
    override val duration: Duration,
    override val legalRestrictions: String,
    override val subjects: Set<Subject>,
    override val searchable: Boolean
) : VideoAssetAttributes {
    override fun toString(): String {
        return "VideoAsset(assetId=$assetId, title='$title', contentPartnerId='$contentPartnerId')"
    }
}

data class PartialVideoAsset (
    override val playbackId: PlaybackId? = null,
    override val title: String? = null,
    override val description: String? = null,
    override val keywords: List<String>? = null,
    override val releasedOn: LocalDate? = null,
    override val contentPartnerId: String? = null,
    override val contentPartnerVideoId: String? = null,
    override val type: LegacyVideoType? = null,
    override val duration: Duration? = null,
    override val legalRestrictions: String? = null,
    override val subjects: Set<Subject>? = null,
    override val searchable: Boolean? = null
) : VideoAssetAttributes
