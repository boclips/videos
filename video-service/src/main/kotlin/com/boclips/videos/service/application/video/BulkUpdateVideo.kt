package com.boclips.videos.service.application.video

import com.boclips.search.service.domain.videos.legacy.LegacyVideoSearchService
import com.boclips.videos.service.application.video.exceptions.InvalidBulkUpdateRequestException
import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.model.video.DeliveryMethod
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.video.VideoAccessService
import com.boclips.videos.service.domain.service.video.VideoSearchService
import com.boclips.videos.service.domain.service.video.VideoToLegacyVideoMetadataConverter
import com.boclips.videos.service.presentation.deliveryMethod.DeliveryMethodResource
import com.boclips.videos.service.presentation.deliveryMethod.DeliveryMethodResourceConverter
import com.boclips.videos.service.presentation.video.BulkUpdateRequest
import com.boclips.videos.service.presentation.video.VideoResourceStatus
import mu.KLogging

open class BulkUpdateVideo(
    private val videoRepository: VideoRepository,
    private val videoSearchService: VideoSearchService,
    private val legacyVideoSearchService: LegacyVideoSearchService,
    private val videoAccessService: VideoAccessService
) {
    companion object : KLogging();

    open operator fun invoke(bulkUpdateRequest: BulkUpdateRequest?) {
        bulkUpdateRequest?.hiddenFromSearchForDeliveryMethods?.let { deliveryMethodResourcesHiddenFromSearch ->
            val deliveryMethods = convertResourcesToDeliveryMethods(deliveryMethodResourcesHiddenFromSearch)
            val videoIds = bulkUpdateRequest.ids.map(::VideoId)

            videoAccessService.setSearchBlacklist(videoIds, deliveryMethods)
            updateDeliveryMethodsInSearch(videoIds, deliveryMethods)
        } ?: updateLegacyRequestStatusInSearch(bulkUpdateRequest)
    }

    private fun convertResourcesToDeliveryMethods(hiddenFromSearchForDeliveryMethods: Set<DeliveryMethodResource>): Set<DeliveryMethod> {
        return hiddenFromSearchForDeliveryMethods.map(DeliveryMethodResourceConverter::fromResource).toSet()
    }

    private fun updateDeliveryMethodsInSearch(
        videoIds: List<VideoId>,
        deliveryMethods: Set<DeliveryMethod>
    ) {
        val videos = videoRepository.findAll(videoIds)

        updateStreamDeliveryMethodInSearch(videos, deliveryMethods)
        updateDownloadDeliveryMethod(videos, deliveryMethods)
    }

    private fun updateDownloadDeliveryMethod(
        videos: List<Video>,
        deliveryMethods: Set<DeliveryMethod>
    ) {
        if (deliveryMethods.contains(DeliveryMethod.DOWNLOAD)) {
            legacyVideoSearchService.bulkRemoveFromSearch(videos.map { it.videoId.value })
        } else {
            legacyVideoSearchService.upsert(videos
                .filter { it.isBoclipsHosted() }
                .map { video -> VideoToLegacyVideoMetadataConverter.convert(video) }
                .asSequence())
        }
    }

    private fun updateStreamDeliveryMethodInSearch(
        videos: List<Video>,
        deliveryMethods: Set<DeliveryMethod>
    ) {
        if (deliveryMethods.contains(DeliveryMethod.STREAM)) {
            videoSearchService.bulkRemoveFromSearch(videos.map { it.videoId.value })
        } else {
            videoSearchService.upsert(videos.asSequence())
        }
    }

    private fun updateLegacyRequestStatusInSearch(bulkUpdateRequest: BulkUpdateRequest?) {
        when (bulkUpdateRequest?.status) {
            VideoResourceStatus.SEARCHABLE -> makeSearchable(bulkUpdateRequest)
            VideoResourceStatus.SEARCH_DISABLED -> disableFromSearch(bulkUpdateRequest)
            null -> throw InvalidBulkUpdateRequestException("Null bulk update request cannot be processed")
        }
    }

    private fun disableFromSearch(bulkUpdateRequest: BulkUpdateRequest) {
        val videoIds = bulkUpdateRequest.ids.map { VideoId(value = it) }

        videoAccessService.revokeAccess(videoIds)
        videoSearchService.bulkRemoveFromSearch(bulkUpdateRequest.ids)
        legacyVideoSearchService.bulkRemoveFromSearch(bulkUpdateRequest.ids)
    }

    private fun makeSearchable(bulkUpdateRequest: BulkUpdateRequest) {
        val videoIds = bulkUpdateRequest.ids.map { VideoId(value = it) }
        videoAccessService.grantAccess(videoIds)

        videoRepository.findAll(videoIds).let { videos ->
            videoSearchService.upsert(videos.asSequence())

            legacyVideoSearchService.upsert(videos
                .filter { it.isBoclipsHosted() }
                .map { video -> VideoToLegacyVideoMetadataConverter.convert(video) }
                .asSequence())
        }
    }
}
