package com.boclips.videos.service.application.video

import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.asset.VideoAsset
import com.boclips.videos.service.domain.model.asset.VideoAssetRepository
import com.boclips.videos.service.domain.model.asset.VideoType
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.presentation.video.CreateVideoRequest
import com.boclips.videos.service.presentation.video.VideoResource

class CreateVideo(
        private val videoAssetRepository: VideoAssetRepository,
        private val getVideoById: GetVideoById
) {
    fun execute(createRequest: CreateVideoRequest): VideoResource {

        val videoAsset = videoAssetRepository.create(VideoAsset(
                assetId = AssetId(""),
                playbackId = PlaybackId(PlaybackProviderType.valueOf(createRequest.playbackProvider!!), createRequest.playbackId!!),
                title = createRequest.title!!,
                description = createRequest.description!!,
                keywords = createRequest.keywords!!,
                releasedOn = createRequest.releasedOn!!,
                contentProvider = createRequest.provider!!,
                contentProviderId = createRequest.providerVideoId!!,
                type = VideoType.valueOf(createRequest.contentType!!),
                duration = createRequest.duration!!,
                legalRestrictions = createRequest.legalRestrictions!!
        ))

        return getVideoById.execute(videoAsset.assetId.value)
    }
}