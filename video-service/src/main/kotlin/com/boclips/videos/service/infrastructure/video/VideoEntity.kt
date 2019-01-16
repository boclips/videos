package com.boclips.videos.service.infrastructure.video

import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.asset.VideoAsset
import com.boclips.videos.service.domain.model.asset.LegacyVideoType
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import org.apache.commons.lang3.time.DurationFormatUtils
import java.time.LocalDate
import javax.persistence.*

@Entity(name = "metadata_orig")
class VideoEntity(
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
        var source: String? = null,
        var namespace: String? = null,
        var title: String? = null,
        var description: String? = null,
        var date: String? = null,
        var duration: String? = null,
        var keywords: String? = null,
        var price_category: String? = null,
        var sounds: String? = null,
        var color: String? = null,
        var location: String? = null,
        var country: String? = null,
        var state: String? = null,
        var city: String? = null,
        var region: String? = null,
        var alternative_id: String? = null,
        var alt_source: String? = null,
        var restrictions: String? = null,
        @Column(name = "unique_id") var uniqueId: String? = null,
        var type_id: Int? = null,
        var reference_id: String? = null,
        var playback_provider: String? = null,
        var playback_id: String? = null
) {

    companion object {
        fun fromVideoAsset(videoAsset: VideoAsset) = VideoEntity(
                id = videoAsset.assetId.value.toLongOrNull(),
                namespace = generateNamespace(videoAsset.contentPartnerId, videoAsset.contentPartnerVideoId),
                title = videoAsset.title,
                description = videoAsset.description,
                date = videoAsset.releasedOn.toString(),
                duration = DurationFormatUtils.formatDuration(videoAsset.duration.toMillis(), "HH:mm:ss", true),
                restrictions = videoAsset.legalRestrictions,
                keywords = videoAsset.keywords.joinToString(),
                type_id = videoAsset.type.id,
                reference_id = videoAsset.playbackId.value,
                playback_id = videoAsset.playbackId.value,
                playback_provider = videoAsset.playbackId.type.name,
                source = videoAsset.contentPartnerId,
                uniqueId = videoAsset.contentPartnerVideoId
        )
    }

    fun toVideoAsset(): VideoAsset {
        return VideoAsset(
                assetId = AssetId(value = id.toString()),
                playbackId = PlaybackId(type = PlaybackProviderType.valueOf(playback_provider!!), value = playback_id!!),
                title = title!!,
                description = description!!,
                releasedOn = LocalDate.parse(date!!),
                contentPartnerId = source!!,
                contentPartnerVideoId = uniqueId!!,
                type = LegacyVideoType.fromId(type_id!!),
                keywords = keywords?.split(",")?.map { it.trim() }.orEmpty(),
                duration = DurationParser.parse(duration),
                legalRestrictions = restrictions.orEmpty(),
                subjects = emptySet()
        )
    }

}
