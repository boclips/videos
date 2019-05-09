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
import java.time.ZoneOffset
import java.util.Date
import java.util.Locale

object VideoDocumentConverter {
    fun toDocument(asset: VideoAsset): VideoDocument {
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
            playback = PlaybackDocument(
                id = asset.playbackId.value,
                type = asset.playbackId.type.name,
                downloadUrl = null,
                dashStreamUrl = null,
                hdsStreamUrl = null,
                progressiveStreamUrl = null,
                duration = null,
                thumbnailUrl = null,
                lastVerified = null
            ),
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

    fun toAsset(document: VideoDocument): VideoAsset {
        return VideoAsset(
            assetId = AssetId(document.id.toHexString()),
            title = document.title,
            description = document.description,
            contentPartnerId = document.source.contentPartner.name,
            contentPartnerVideoId = document.source.videoReference,
            playbackId = PlaybackId(
                type = PlaybackProviderType.valueOf(document.playback.type),
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

    fun toPlaybackDocument(videoPlayback: VideoPlayback): PlaybackDocument {
        return when (videoPlayback) {
            is StreamPlayback -> PlaybackDocument(
                id = videoPlayback.id.value,
                type = "KALTURA",
                thumbnailUrl = null,
                downloadUrl = null,
                hdsStreamUrl = null,
                dashStreamUrl = null,
                progressiveStreamUrl = null,
                lastVerified = null,
                duration = null
            )
            is YoutubePlayback -> PlaybackDocument(
                id = videoPlayback.id.value,
                type = "YOUTUBE",
                thumbnailUrl = null,
                downloadUrl = null,
                hdsStreamUrl = null,
                dashStreamUrl = null,
                progressiveStreamUrl = null,
                lastVerified = null,
                duration = null
            )
            else -> throw IllegalStateException("Stream format not recognised.")
        }
    }

    fun toPlayback(playbackDocument: PlaybackDocument): VideoPlayback {
        TODO()
    }
}