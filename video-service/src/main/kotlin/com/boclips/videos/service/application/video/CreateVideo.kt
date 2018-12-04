package com.boclips.videos.service.application.video

import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.asset.VideoAsset
import com.boclips.videos.service.domain.model.asset.VideoAssetRepository
import com.boclips.videos.service.domain.model.asset.VideoType
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.presentation.video.CreateVideoRequest

class CreateVideo(
        private val videoAssetRepository: VideoAssetRepository
) {
    fun execute(video: CreateVideoRequest): AssetId {
        val videoAsset = VideoAsset(
                assetId = AssetId(""),
                playbackId = PlaybackId(PlaybackProviderType.valueOf(video.playbackProvider!!), video.playbackId!!),
                title = video.title!!,
                description = video.description!!,
                keywords = video.keywords!!,
                releasedOn = video.releasedOn!!,
                contentProvider = video.provider!!,
                contentProviderId = video.providerVideoId!!,
                type = VideoType.valueOf(video.contentType!!),
                duration = video.duration!!,
                legalRestrictions = video.legalRestrictions!!
        )

        return videoAssetRepository.create(videoAsset).assetId
    }
}