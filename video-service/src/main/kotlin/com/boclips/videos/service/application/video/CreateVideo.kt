package com.boclips.videos.service.application.video

import com.boclips.videos.service.application.video.exceptions.VideoPlaybackNotFound
import com.boclips.videos.service.domain.model.asset.VideoAssetRepository
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.playback.PlaybackRespository
import com.boclips.videos.service.domain.service.SearchService
import com.boclips.videos.service.presentation.video.CreateVideoRequest
import com.boclips.videos.service.presentation.video.CreateVideoRequestToAssetConverter
import com.boclips.videos.service.presentation.video.VideoResource
import io.micrometer.core.instrument.Counter

class CreateVideo(
        private val videoAssetRepository: VideoAssetRepository,
        private val getVideoById: GetVideoById,
        private val createVideoRequestToAssetConverter: CreateVideoRequestToAssetConverter,
        private val searchServiceAdmin: SearchService,
        private val playbackRepository: PlaybackRespository,
        private val videoCounter: Counter

) {
    fun execute(createRequest: CreateVideoRequest): VideoResource {
        val assetToBeCreated = createVideoRequestToAssetConverter.convert(createRequest)
        ensureVideoPlaybackExists(createRequest)

        val createdAsset = videoAssetRepository.create(assetToBeCreated)
        searchServiceAdmin.upsert(sequenceOf(createdAsset))

        videoCounter.increment()

        return getVideoById.execute(createdAsset.assetId.value)
    }

    private fun ensureVideoPlaybackExists(createRequest: CreateVideoRequest) {
        (playbackRepository.find(PlaybackId(type = PlaybackProviderType.valueOf(createRequest.playbackProvider!!), value = createRequest.playbackId!!))
                ?: throw VideoPlaybackNotFound("Video playback for asset ${createRequest.playbackId} not found in ${createRequest.playbackProvider}"))
    }
}