package com.boclips.videos.service.application.video.search

import com.boclips.videos.service.application.video.exceptions.QueryValidationException
import com.boclips.videos.service.application.video.exceptions.VideoAssetNotFoundException
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.asset.VideoAssetRepository
import com.boclips.videos.service.domain.service.VideoService
import com.boclips.videos.service.presentation.video.VideoResource
import com.boclips.videos.service.presentation.video.VideoToResourceConverter

class GetVideoById(
        private val videoService: VideoService,
        private val videoToResourceConverter: VideoToResourceConverter,
        private val videoAssetRepository: VideoAssetRepository
) {
    companion object {
        fun isAlias(potentialAlias: String): Boolean = Regex("\\d+").matches(potentialAlias)
    }

    fun execute(videoId: String?): VideoResource {
        videoId ?: throw QueryValidationException()

        val assetId = if (isAlias(videoId)) {
            videoAssetRepository.resolveAlias(videoId) ?: throw VideoAssetNotFoundException()
        } else {
            AssetId(value = videoId)
        }

        return videoService.get(assetId)
                .let(videoToResourceConverter::convert)
    }
}