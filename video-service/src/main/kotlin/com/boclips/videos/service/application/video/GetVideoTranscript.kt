package com.boclips.videos.service.application.video

import com.boclips.videos.service.application.video.exceptions.VideoAssetNotFoundException
import com.boclips.videos.service.application.video.exceptions.VideoTranscriptNotFound
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.asset.VideoAssetRepository

class GetVideoTranscript(
    private val videoAssetRepository: VideoAssetRepository
    ) {
    operator fun invoke(id: String?) : String {
        if (id == null || id.isBlank()) {
            throw VideoAssetNotFoundException()
        }

        val assetId = AssetId(value = id)
        val videoAsset = videoAssetRepository.find(assetId) ?: throw VideoAssetNotFoundException(assetId)

        if (videoAsset.transcript == null) {
            throw VideoTranscriptNotFound(assetId)
        }

        return videoAsset.transcript
    }

}