package com.boclips.videos.service.application.video

import com.boclips.search.service.domain.legacy.LegacySearchService
import com.boclips.videos.service.application.video.exceptions.VideoAssetExists
import com.boclips.videos.service.application.video.exceptions.VideoPlaybackNotFound
import com.boclips.videos.service.domain.model.asset.VideoAsset
import com.boclips.videos.service.domain.model.asset.VideoAssetRepository
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.playback.PlaybackRespository
import com.boclips.videos.service.domain.service.SearchService
import com.boclips.videos.service.domain.service.VideoAssetToLegacyVideoMetadataConverter
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
        private val videoCounter: Counter,
        private val legacySearchService: LegacySearchService
) {
    fun execute(createRequest: CreateVideoRequest): VideoResource {
        val assetToBeCreated = createVideoRequestToAssetConverter.convert(createRequest)
        ensureVideoPlaybackExists(createRequest)
        ensureVideoIsUnique(assetToBeCreated)

        val createdAsset = videoAssetRepository.create(assetToBeCreated)
        searchServiceAdmin.upsert(sequenceOf(createdAsset))
        legacySearchService.upsert(sequenceOf(VideoAssetToLegacyVideoMetadataConverter.convert(createdAsset)))

        videoCounter.increment()

        return getVideoById.execute(createdAsset.assetId.value)
    }

    private fun ensureVideoIsUnique(asset: VideoAsset) {
        if (videoAssetRepository.existsVideoFromContentPartner(asset.contentPartnerId, asset.contentPartnerVideoId)) {
            throw VideoAssetExists(asset.contentPartnerId, asset.contentPartnerVideoId)
        }
    }

    private fun ensureVideoPlaybackExists(createRequest: CreateVideoRequest) {
        (playbackRepository.find(PlaybackId(type = PlaybackProviderType.valueOf(createRequest.playbackProvider!!), value = createRequest.playbackId!!))
                ?: throw VideoPlaybackNotFound("Video playback for asset ${createRequest.playbackId} not found in ${createRequest.playbackProvider}"))
    }
}
