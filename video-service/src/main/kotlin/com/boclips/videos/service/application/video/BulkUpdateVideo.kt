package com.boclips.videos.service.application.video

import com.boclips.videos.service.application.video.exceptions.InvalidBulkUpdateRequestException
import com.boclips.videos.service.application.video.search.ExcludeVideosFromSearchForDownload
import com.boclips.videos.service.application.video.search.ExcludeVideosFromSearchForStream
import com.boclips.videos.service.application.video.search.IncludeVideosInSearchForDownload
import com.boclips.videos.service.application.video.search.IncludeVideosInSearchForStream
import com.boclips.videos.service.domain.model.video.DeliveryMethod
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.video.VideoAccessService
import com.boclips.videos.service.presentation.deliveryMethod.DeliveryMethodResource
import com.boclips.videos.service.presentation.deliveryMethod.DeliveryMethodResourceConverter
import com.boclips.videos.service.presentation.video.BulkUpdateRequest
import com.boclips.videos.service.presentation.video.VideoResourceStatus
import mu.KLogging

open class BulkUpdateVideo(
    private val videoAccessService: VideoAccessService,
    private val includeVideosInSearchForStream: IncludeVideosInSearchForStream,
    private val excludeVideosFromSearchForStream: ExcludeVideosFromSearchForStream,
    private val excludeVideosFromSearchForDownload: ExcludeVideosFromSearchForDownload,
    private val includeVideosInSearchForDownload: IncludeVideosInSearchForDownload
) {
    companion object : KLogging();

    open operator fun invoke(bulkUpdateRequest: BulkUpdateRequest?) {
        bulkUpdateRequest?.hiddenFromSearchForDeliveryMethods?.let { deliveryMethodResourcesHiddenFromSearch ->
            val deliveryMethods = convertResourcesToDeliveryMethods(deliveryMethodResourcesHiddenFromSearch)
            val videoIds = bulkUpdateRequest.ids.map(::VideoId)

            videoAccessService.setSearchBlacklist(videoIds, deliveryMethods)
            updateDeliveryMethodsInSearch(bulkUpdateRequest.ids, deliveryMethods)
        } ?: updateLegacyRequestStatusInSearch(bulkUpdateRequest)
    }

    private fun convertResourcesToDeliveryMethods(hiddenFromSearchForDeliveryMethods: Set<DeliveryMethodResource>): Set<DeliveryMethod> {
        return hiddenFromSearchForDeliveryMethods.map(DeliveryMethodResourceConverter::fromResource).toSet()
    }

    private fun updateDeliveryMethodsInSearch(
        videoIds: List<String>,
        deliveryMethods: Set<DeliveryMethod>
    ) {
        updateStreamDeliveryMethodInSearch(deliveryMethods, videoIds)
        updateDownloadDeliveryMethod(deliveryMethods, videoIds)
    }

    private fun updateDownloadDeliveryMethod(
        deliveryMethods: Set<DeliveryMethod>,
        videoIds: List<String>
    ) {
        if (deliveryMethods.contains(DeliveryMethod.DOWNLOAD)) {
            excludeVideosFromSearchForDownload.invoke(videoIds = videoIds)
        } else {
            includeVideosInSearchForDownload.invoke(videoIds = videoIds)
        }
    }

    private fun updateStreamDeliveryMethodInSearch(
        deliveryMethods: Set<DeliveryMethod>,
        videoIds: List<String>
    ) {
        if (deliveryMethods.contains(DeliveryMethod.STREAM)) {
            excludeVideosFromSearchForStream.invoke(videoIds = videoIds)
        } else {
            includeVideosInSearchForStream.invoke(videoIds = videoIds)
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

        excludeVideosFromSearchForDownload.invoke(bulkUpdateRequest.ids)
        excludeVideosFromSearchForStream.invoke(bulkUpdateRequest.ids)
    }

    private fun makeSearchable(bulkUpdateRequest: BulkUpdateRequest) {
        val videoIds = bulkUpdateRequest.ids.map { VideoId(value = it) }
        videoAccessService.grantAccess(videoIds)

        includeVideosInSearchForDownload.invoke(bulkUpdateRequest.ids)
        includeVideosInSearchForStream.invoke(bulkUpdateRequest.ids)
    }
}
