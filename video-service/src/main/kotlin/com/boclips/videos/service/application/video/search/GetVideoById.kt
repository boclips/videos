package com.boclips.videos.service.application.video.search

import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.asset.VideoAssetRepository
import com.boclips.videos.service.domain.service.VideoService
import com.boclips.videos.service.presentation.video.VideoResource
import com.boclips.videos.service.presentation.video.VideoToResourceConverter

class GetVideoById(
    private val videoService: VideoService,
    private val videoToResourceConverter: VideoToResourceConverter
) {

    operator fun invoke(assetId: AssetId): VideoResource {
        return videoService.get(assetId)
            .let(videoToResourceConverter::convert)
    }
}