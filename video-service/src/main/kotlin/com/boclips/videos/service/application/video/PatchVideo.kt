package com.boclips.videos.service.application.video

import com.boclips.videos.service.application.video.exceptions.VideoAssetNotFoundException
import com.boclips.videos.service.application.video.search.SearchVideo
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.asset.VideoAssetRepository
import com.boclips.videos.service.presentation.video.VideoResource

class PatchVideo(
    private val videoAssetRepository: VideoAssetRepository
) {
    operator fun invoke(id: String?, patch: VideoResource) {
        val assetId = try {
            resolveToAssetId(id)
        } catch (ex: Exception) {
            throw VideoAssetNotFoundException()
        }

        val updateCommands = VideoUpdatesConverter.convert(assetId, patch)

        videoAssetRepository.bulkUpdate(updateCommands)
    }

    private fun resolveToAssetId(idOrAlias: String?): AssetId {
        if (idOrAlias == null) throw VideoAssetNotFoundException()

        return if (SearchVideo.isAlias(idOrAlias)) {
            videoAssetRepository.resolveAlias(idOrAlias) ?: throw VideoAssetNotFoundException()
        } else {
            videoAssetRepository.find(AssetId(value = idOrAlias))?.assetId
                ?: throw VideoAssetNotFoundException()
        }
    }
}