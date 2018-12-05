package com.boclips.videos.service.application.video

import com.boclips.videos.service.domain.model.asset.VideoAssetRepository
import com.boclips.videos.service.domain.service.SearchService
import com.boclips.videos.service.presentation.video.CreateVideoRequest
import com.boclips.videos.service.presentation.video.CreateVideoRequestToAssetConverter
import com.boclips.videos.service.presentation.video.VideoResource

class CreateVideo(
        private val videoAssetRepository: VideoAssetRepository,
        private val getVideoById: GetVideoById,
        private val createVideoRequestToAssetConverter: CreateVideoRequestToAssetConverter,
        private val searchService: SearchService
) {
    fun execute(createRequest: CreateVideoRequest): VideoResource {
        val assetToBeCreated = createVideoRequestToAssetConverter.convert(createRequest)
        val createdAsset = videoAssetRepository.create(assetToBeCreated)
        searchService.upsert(sequenceOf(createdAsset))
        return getVideoById.execute(createdAsset.assetId.value)
    }
}