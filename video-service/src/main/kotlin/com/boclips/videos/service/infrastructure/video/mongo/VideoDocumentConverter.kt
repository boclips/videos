package com.boclips.videos.service.infrastructure.video.mongo

import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.asset.LegacyVideoType
import com.boclips.videos.service.domain.model.asset.Subject
import com.boclips.videos.service.domain.model.asset.VideoAsset
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import org.litote.kmongo.set
import java.time.Duration
import java.time.ZoneOffset
import java.util.Date

object VideoDocumentConverter {
    fun toDocument(asset: VideoAsset): VideoDocument {
        return VideoDocument(
            id = ObjectId(asset.assetId.value),
            title = asset.title,
            description = asset.description,
            source = VideoDocument.Source(
                contentPartner = VideoDocument.Source.ContentPartner(name = asset.contentPartnerId),
                videoReference = asset.contentPartnerVideoId
            ),
            playback = VideoDocument.Playback(id = asset.playbackId.value, type = asset.playbackId.type.name),
            legacy = VideoDocument.Legacy(type = asset.type.name),
            keywords = asset.keywords,
            subjects = asset.subjects.map(Subject::name),
            releaseDate = Date.from(asset.releasedOn.atStartOfDay().toInstant(ZoneOffset.UTC)),
            durationSeconds = asset.duration.seconds.toInt(),
            legalRestrictions = asset.legalRestrictions,
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
                type = LegacyVideoType.valueOf(document.legacy.type),
                keywords = document.keywords,
                subjects = document.subjects.map(::Subject).toSet(),
                releasedOn = document.releaseDate.toInstant().atOffset(ZoneOffset.UTC).toLocalDate(),
                duration = Duration.ofSeconds(document.durationSeconds.toLong()),
                legalRestrictions = document.legalRestrictions,
                searchable = document.searchable
            )
    }

    fun durationToDocument(duration: Duration): Bson {
        return set(VideoDocument::durationSeconds, duration.seconds.toInt())
    }

    fun subjectsToDocument(subjects: List<Subject>): Bson {
        return set(VideoDocument::subjects, subjects.map { it.name })
    }
}