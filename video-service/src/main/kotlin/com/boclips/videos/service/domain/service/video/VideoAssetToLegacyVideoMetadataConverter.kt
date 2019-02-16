package com.boclips.videos.service.domain.service.video

import com.boclips.search.service.domain.legacy.LegacyVideoMetadata
import com.boclips.videos.service.domain.model.asset.VideoAsset

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
            videoTypeTitle = asset.type.title
        )
    }
}