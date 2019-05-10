package com.boclips.videos.service.infrastructure.video.mongo

import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.asset.LegacySubject
import com.boclips.videos.service.domain.model.asset.LegacyVideoType
import com.boclips.videos.service.domain.model.asset.VideoAsset
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.playback.StreamPlayback
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.domain.model.playback.YoutubePlayback
import org.bson.types.ObjectId
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.util.Date
import java.util.Locale

object VideoDocumentConverter {
    fun toVideoDocument(asset: VideoAsset): VideoDocument {
        return VideoDocument(
            id = ObjectId(asset.assetId.value),
            title = asset.title,
            description = asset.description,
            source = SourceDocument(
                contentPartner = ContentPartnerDocument(
                    name = asset.contentPartnerId
                ),
                videoReference = asset.contentPartnerVideoId
            ),
            playback = toPlaybackDocument(videoPlayback = asset.playback, legacyPlaybackId = asset.playbackId),
            legacy = LegacyDocument(type = asset.type.name),
            keywords = asset.keywords,
            subjects = asset.subjects.map(LegacySubject::name),
            releaseDate = Date.from(asset.releasedOn.atStartOfDay().toInstant(ZoneOffset.UTC)),
            durationSeconds = asset.duration.seconds.toInt(),
            legalRestrictions = asset.legalRestrictions,
            language = asset.language?.toLanguageTag(),
            transcript = asset.transcript,
            topics = asset.topics.map(TopicDocumentConverter::toDocument),
            searchable = asset.searchable
        )
    }

    fun toVideoAsset(document: VideoDocument): VideoAsset {
        return VideoAsset(
            assetId = AssetId(document.id.toHexString()),
            title = document.title,
            description = document.description,
            contentPartnerId = document.source.contentPartner.name,
            contentPartnerVideoId = document.source.videoReference,
            playbackId = PlaybackId(
                type = PlaybackProviderType.valueOf(document.playback!!.type),
                value = document.playback.id
            ),
            playback = null,
            type = LegacyVideoType.valueOf(document.legacy.type),
            keywords = document.keywords,
            subjects = document.subjects.map(::LegacySubject).toSet(),
            releasedOn = document.releaseDate.toInstant().atOffset(ZoneOffset.UTC).toLocalDate(),
            duration = Duration.ofSeconds(document.durationSeconds.toLong()),
            legalRestrictions = document.legalRestrictions,
            language = document.language?.let(Locale::forLanguageTag),
            transcript = document.transcript,
            topics = document.topics.orEmpty().map(TopicDocumentConverter::toTopic).toSet(),
            searchable = document.searchable
        )
    }

    fun toPlaybackDocument(
        videoPlayback: VideoPlayback?,
        legacyPlaybackId: PlaybackId
    ): PlaybackDocument {
        if (videoPlayback == null) {
            return PlaybackDocument(
                id = legacyPlaybackId.value,
                type = legacyPlaybackId.type.name,
                thumbnailUrl = null,
                downloadUrl = null,
                hlsStreamUrl = null,
                dashStreamUrl = null,
                progressiveStreamUrl = null,
                lastVerified = null,
                duration = null
            )
        }

        return when (videoPlayback) {
            is StreamPlayback -> PlaybackDocument(
                id = videoPlayback.id.value,
                type = "KALTURA",
                thumbnailUrl = listOf(videoPlayback.thumbnailUrl),
                downloadUrl = videoPlayback.downloadUrl,
                hlsStreamUrl = videoPlayback.appleHlsStreamUrl,
                dashStreamUrl = videoPlayback.mpegDashStreamUrl,
                progressiveStreamUrl = videoPlayback.progressiveDownloadStreamUrl,
                lastVerified = Instant.now(),
                duration = videoPlayback.duration.seconds.toInt()
            )
            is YoutubePlayback -> PlaybackDocument(
                id = videoPlayback.id.value,
                type = "YOUTUBE",
                thumbnailUrl = listOf(videoPlayback.thumbnailUrl),
                downloadUrl = null,
                hlsStreamUrl = null,
                dashStreamUrl = null,
                progressiveStreamUrl = null,
                lastVerified = Instant.now(),
                duration = videoPlayback.duration.seconds.toInt()
            )
            else -> throw IllegalStateException("Stream format not recognised.")
        }
    }

    fun toPlayback(playbackDocument: PlaybackDocument): VideoPlayback {
        val thumbnailUrl = playbackDocument.thumbnailUrl!!.first()
        val duration = Duration.ofSeconds(playbackDocument.duration!!.toLong())

        return when (playbackDocument.type) {
            "KALTURA" -> {
                StreamPlayback(
                    id = PlaybackId(type = PlaybackProviderType.KALTURA, value = playbackDocument.id),
                    thumbnailUrl = thumbnailUrl,
                    duration = duration,
                    appleHlsStreamUrl = playbackDocument.hlsStreamUrl!!,
                    mpegDashStreamUrl = playbackDocument.dashStreamUrl!!,
                    progressiveDownloadStreamUrl = playbackDocument.progressiveStreamUrl!!,
                    downloadUrl = playbackDocument.downloadUrl!!
                )
            }
            "YOUTUBE" -> YoutubePlayback(
                id = PlaybackId(type = PlaybackProviderType.YOUTUBE, value = playbackDocument.id),
                thumbnailUrl = thumbnailUrl,
                duration = duration
            )
            else -> throw java.lang.IllegalStateException("Could not find video provider ${playbackDocument.type}")
        }
    }
}