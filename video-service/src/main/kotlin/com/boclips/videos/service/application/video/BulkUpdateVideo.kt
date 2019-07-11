package com.boclips.videos.service.application.video

import com.boclips.videos.service.application.video.exceptions.InvalidBulkUpdateRequestException
import com.boclips.videos.service.application.video.search.ExcludeVideosFromSearchForDownload
import com.boclips.videos.service.application.video.search.ExcludeVideosFromSearchForStream
import com.boclips.videos.service.application.video.search.IncludeVideosInSearchForDownload
import com.boclips.videos.service.application.video.search.IncludeVideosInSearchForStream
import com.boclips.videos.service.domain.model.video.DistributionMethod
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.video.VideoAccessService
import com.boclips.videos.service.presentation.deliveryMethod.DeliveryMethodResource
import com.boclips.videos.service.presentation.deliveryMethod.DeliveryMethodResourceConverter
import com.boclips.videos.service.presentation.video.BulkUpdateRequest
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
        } ?: throw InvalidBulkUpdateRequestException("Null bulk update request cannot be processed")
    }

    private fun convertResourcesToDeliveryMethods(hiddenFromSearchForDeliveryMethods: Set<DeliveryMethodResource>): Set<DistributionMethod> {
        return hiddenFromSearchForDeliveryMethods.map(DeliveryMethodResourceConverter::fromResource).toSet()
    }

    private fun updateDeliveryMethodsInSearch(
        videoIds: List<String>,
        distributionMethods: Set<DistributionMethod>
    ) {
        updateStreamDeliveryMethodInSearch(distributionMethods, videoIds)
        updateDownloadDeliveryMethod(distributionMethods, videoIds)
    }

    private fun updateDownloadDeliveryMethod(
        distributionMethods: Set<DistributionMethod>,
        videoIds: List<String>
    ) {
        if (distributionMethods.contains(DistributionMethod.DOWNLOAD)) {
            excludeVideosFromSearchForDownload.invoke(videoIds = videoIds)
        } else {
            includeVideosInSearchForDownload.invoke(videoIds = videoIds)
        }
    }

    private fun updateStreamDeliveryMethodInSearch(
        distributionMethods: Set<DistributionMethod>,
        videoIds: List<String>
    ) {
        if (distributionMethods.contains(DistributionMethod.STREAM)) {
            excludeVideosFromSearchForStream.invoke(videoIds = videoIds)
        } else {
            includeVideosInSearchForStream.invoke(videoIds = videoIds)
        }
    }
}
