package com.boclips.videos.service.infrastructure.video.mongo

import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.asset.LegacyVideoType
import com.boclips.videos.service.domain.model.asset.Subject
import com.boclips.videos.service.domain.model.asset.VideoAsset
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import org.bson.Document
import org.bson.types.ObjectId
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.*

object VideoDocumentConverter {
    fun toDocument(video: VideoAsset): Document {
        return Document()
                .append("_id", ObjectId(video.assetId.value))
                .append("title", video.title)
                .append("description", video.description)
                .append("source",
                        mapOf(
                                "contentPartner" to mapOf(
                                        "name" to video.contentPartnerId
                                ),
                                "videoReference" to video.contentPartnerVideoId
                        )
                )
                .append("playback",
                        mapOf(
                                "id" to video.playbackId.value,
                                "type" to video.playbackId.type.name
                        )
                )
                .append("legacy",
                        mapOf(
                                "type" to video.type.name
                        )
                )
                .append("keywords", video.keywords)
                .append("subjects", video.subjects.map { it.name })
                .append("releaseDate", Date.from(video.releasedOn.atStartOfDay().toInstant(ZoneOffset.UTC)))
                .append("durationSeconds", video.duration.seconds.toInt())
                .append("legalRestrictions", video.legalRestrictions)
                .append("searchable", video.searchable)
    }

    fun fromDocument(document: Document) = VideoFieldExtractor(document).let {
        VideoAsset(
                assetId = it.id(),
                title = it.title(),
                description = it.description(),
                playbackId = it.playbackId(),
                keywords = it.keywords(),
                releasedOn = it.releaseDate(),
                contentPartnerId = it.contentPartnerName(),
                contentPartnerVideoId = it.contentPartnerVideoId(),
                type = it.legacyType(),
                duration = it.duration(),
                legalRestrictions = it.legalRestrictions(),
                subjects = it.subjects(),
                searchable = it.searchable()
        )
    }
}

private class VideoFieldExtractor(val document: Document) {
    fun id() = AssetId(document.getObjectId("_id").toHexString())

    fun title() = document.getString("title")

    fun description() = document.getString("description")

    fun playbackId(): PlaybackId {
        val playbackJson = document.getMap<String>("playback")
        return PlaybackId(
                type = PlaybackProviderType.valueOf(playbackJson["type"] as String),
                value = playbackJson["id"] as String
        )
    }

    fun keywords() = document.getList<String>("keywords")

    fun releaseDate() = document.getLocalDate("releaseDate")

    fun contentPartnerName(): String {
        val contentPartner = source()["contentPartner"] as Map<*, *>
        return contentPartner["name"] as String
    }

    fun contentPartnerVideoId(): String {
        return source()["videoReference"] as String
    }

    fun legacyType(): LegacyVideoType {
        val legacy = document.getMap<String>("legacy")
        val typeName = legacy["type"] as String
        return LegacyVideoType.valueOf(typeName)
    }

    fun duration() = Duration.ofSeconds(document.getInteger("durationSeconds").toLong())

    fun legalRestrictions() = document.getString("legalRestrictions")

    fun subjects() = document.getList<String>("subjects").map { Subject(it) }.toSet()

    fun searchable() = document.getBoolean("searchable")

    private fun source() = document.getMap<Any>("source")
}

@Suppress("UNCHECKED_CAST")
fun <T> Document.getMap(key: String): Map<String, T> {
    return this.get(key, Map::class.java)
            .mapKeys { it.key as String }
            .mapValues { it.value as T }
}

@Suppress("UNCHECKED_CAST")
fun <T> Document.getList(key: String): List<T> {
    return this.get(key, List::class.java)
            .map { it as T }
}

fun Document.getLocalDate(key: String): LocalDate {
    return this.get(key, Date::class.java).toInstant().atOffset(ZoneOffset.UTC).toLocalDate()
}