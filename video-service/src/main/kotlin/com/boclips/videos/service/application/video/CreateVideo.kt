package com.boclips.videos.service.application.video

import com.boclips.search.service.domain.legacy.LegacySearchService
import com.boclips.videos.service.application.video.exceptions.VideoAssetExists
import com.boclips.videos.service.application.video.exceptions.VideoPlaybackNotFound
import com.boclips.videos.service.application.video.search.SearchVideo
import com.boclips.videos.service.domain.exceptions.VideoNotAnalysableException
import com.boclips.videos.service.domain.model.asset.VideoAsset
import com.boclips.videos.service.domain.model.asset.VideoAssetRepository
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.playback.PlaybackRepository
import com.boclips.videos.service.domain.model.playback.VideoPlayback
import com.boclips.videos.service.domain.service.video.SearchService
import com.boclips.videos.service.domain.service.video.VideoAssetToLegacyVideoMetadataConverter
import com.boclips.videos.service.presentation.video.CreateVideoRequest
import com.boclips.videos.service.presentation.video.CreateVideoRequestToAssetConverter
import com.boclips.videos.service.presentation.video.VideoResource
import io.micrometer.core.instrument.Counter
import mu.KLogging
import org.springframework.hateoas.Resource

class CreateVideo(
    private val videoAssetRepository: VideoAssetRepository,
    private val searchVideo: SearchVideo,
    private val createVideoRequestToAssetConverter: CreateVideoRequestToAssetConverter,
    private val searchServiceAdmin: SearchService,
    private val playbackRepository: PlaybackRepository,
    private val videoCounter: Counter,
    private val legacySearchService: LegacySearchService,
    private val analyseVideo: AnalyseVideo
) {
    companion object : KLogging()

    operator fun invoke(createRequest: CreateVideoRequest): Resource<VideoResource> {
        val videoPlayback = getPlayback(createRequest)
        val assetToBeCreated = createVideoRequestToAssetConverter.convert(createRequest, videoPlayback)

        ensureVideoIsUnique(assetToBeCreated)

        val createdAsset = videoAssetRepository.create(assetToBeCreated)

        searchServiceAdmin.upsert(sequenceOf(createdAsset), null)

        if (assetToBeCreated.playbackId.type == PlaybackProviderType.KALTURA) {
            legacySearchService.upsert(sequenceOf(VideoAssetToLegacyVideoMetadataConverter.convert(createdAsset)), null)
        }

        if (createRequest.analyseVideo) {
            try {
                analyseVideo(createdAsset.assetId.value, null)
            } catch (exception: VideoNotAnalysableException) {
                logger.info { "Video cannot be analysed" }
            }
        }

        videoCounter.increment()

        return searchVideo.byId(createdAsset.assetId.value)
    }

    private fun ensureVideoIsUnique(asset: VideoAsset) {
        if (videoAssetRepository.existsVideoFromContentPartner(asset.contentPartnerId, asset.contentPartnerVideoId)) {
            throw VideoAssetExists(asset.contentPartnerId, asset.contentPartnerVideoId)
        }
    }

    private fun getPlayback(createRequest: CreateVideoRequest): VideoPlayback {
        return playbackRepository.find(buildPlaybackId(createRequest)) ?: throw VideoPlaybackNotFound(createRequest)
    }

    private fun buildPlaybackId(createRequest: CreateVideoRequest): PlaybackId {
        if (createRequest.playbackId != null && createRequest.playbackProvider != null) {
            return PlaybackId(
                type = PlaybackProviderType.valueOf(createRequest.playbackProvider),
                value = createRequest.playbackId
            )
        } else {
            throw VideoPlaybackNotFound(createRequest)
        }
    }
}
