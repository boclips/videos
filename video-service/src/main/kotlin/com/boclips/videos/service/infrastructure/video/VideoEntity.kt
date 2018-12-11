package com.boclips.videos.service.infrastructure.video

import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.asset.VideoAsset
import com.boclips.videos.service.domain.model.asset.VideoType
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import org.springframework.data.annotation.Id
import java.time.LocalDate

class VideoEntity(
        @Id var id: Long,
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
        var unique_id: String? = null,
        var type_id: Int? = null,
        var reference_id: String? = null,
        var playback_provider: String? = null,
        var playback_id: String? = null
) {

    fun toVideoAsset(): VideoAsset {
        return VideoAsset(
                assetId = AssetId(value = id.toString()),
                playbackId = PlaybackId(type = PlaybackProviderType.valueOf(playback_provider!!), value = playback_id!!),
                title = title!!,
                description = description!!,
                releasedOn = LocalDate.parse(date!!),
                contentPartnerId = source!!,
                contentPartnerVideoId =  unique_id!!,
                type = VideoType.fromId(type_id!!),
                keywords = keywords?.split(",")?.map { it.trim() }.orEmpty(),
                duration = DurationParser.parse(duration),
                legalRestrictions = restrictions.orEmpty(),
                subjects = emptySet()
        )
    }

}
