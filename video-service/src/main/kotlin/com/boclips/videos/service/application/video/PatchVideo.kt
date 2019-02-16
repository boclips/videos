package com.boclips.videos.service.application.video

import com.boclips.videos.service.application.video.exceptions.VideoAssetNotFoundException
import com.boclips.videos.service.application.video.search.SearchVideo
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.asset.VideoAssetRepository
import com.boclips.videos.service.presentation.video.VideoResource
import com.boclips.videos.service.presentation.video.VideoResourceToPartialVideoAssetConverter

class PatchVideo(
    private val videoAssetRepository: VideoAssetRepository
) {
    operator fun invoke(id: String?, patch: VideoResource) {
        val assetId = try {
            resolveToAssetId(id)
        } catch (ex: Exception) {
            throw VideoAssetNotFoundException()
        }

        val updateCommands = VideoResourceToPartialVideoAssetConverter
            .convert(patch.copy(id = assetId.value))

        videoAssetRepository.update(updateCommands.first())
    }

    private fun resolveToAssetId(videoIdParam: String?): AssetId {
        if (videoIdParam == null) throw VideoAssetNotFoundException()

        return if (SearchVideo.isAlias(videoIdParam)) {
            videoAssetRepository.resolveAlias(videoIdParam) ?: throw VideoAssetNotFoundException()
        } else {
            AssetId(value = videoIdParam)
        }
    }
}