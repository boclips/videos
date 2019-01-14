package com.boclips.videos.service.domain.service

import com.boclips.search.service.domain.legacy.LegacyVideoMetadata
import com.boclips.search.service.domain.legacy.LegacyVideoType
import com.boclips.videos.service.domain.model.asset.VideoAsset
import com.boclips.videos.service.domain.model.asset.VideoType
import org.bouncycastle.asn1.x500.style.RFC4519Style.description

object VideoAssetToLegacyVideoMetadataConverter {

    fun convert(asset: VideoAsset): LegacyVideoMetadata {
        return LegacyVideoMetadata(
                id = asset.assetId.value,
                title = asset.title,
                description = asset.description,
                keywords = asset.keywords,
                duration = asset.duration,
                contentPartnerName = asset.contentPartnerId,
                contentPartnerVideoId = asset.contentPartnerVideoId,
                releaseDate = asset.releasedOn,
                videoType = convertVideoType(asset.type)
        )
    }

    fun convertVideoType(videoType: VideoType): LegacyVideoType {
        return LegacyVideoType.valueOf(videoType.name)
    }

}