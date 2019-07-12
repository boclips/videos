package com.boclips.videos.service.application.video

import com.boclips.videos.service.application.video.exceptions.InvalidBulkUpdateRequestException
import com.boclips.videos.service.application.video.search.ExcludeVideosFromSearchForDownload
import com.boclips.videos.service.application.video.search.ExcludeVideosFromSearchForStream
import com.boclips.videos.service.application.video.search.IncludeVideosInSearchForDownload
import com.boclips.videos.service.application.video.search.IncludeVideosInSearchForStream
import com.boclips.videos.service.domain.model.video.DistributionMethod
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.video.VideoAccessService
import com.boclips.videos.service.presentation.deliveryMethod.DistributionMethodResource
import com.boclips.videos.service.presentation.deliveryMethod.DistributionMethodResourceConverter
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
        bulkUpdateRequest?.distributionMethods?.let { distributionMethodsResource ->
            val distributionMethods = convertResourcesToDistributionMethods(distributionMethodsResource)
            val videoIds = bulkUpdateRequest.ids.map(::VideoId)

            videoAccessService.setSearchBlacklist(videoIds, distributionMethods)
            updateDeliveryMethodsInSearch(bulkUpdateRequest.ids, distributionMethods)
        } ?: throw InvalidBulkUpdateRequestException("Null bulk update request cannot be processed")
    }

    private fun convertResourcesToDistributionMethods(hiddenFromSearchForDistributionMethods: Set<DistributionMethodResource>): Set<DistributionMethod> {
        return hiddenFromSearchForDistributionMethods.map(DistributionMethodResourceConverter::fromResource).toSet()
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
            includeVideosInSearchForDownload.invoke(videoIds = videoIds)
        } else {
            excludeVideosFromSearchForDownload.invoke(videoIds = videoIds)
        }
    }

    private fun updateStreamDeliveryMethodInSearch(
        distributionMethods: Set<DistributionMethod>,
        videoIds: List<String>
    ) {
        if (distributionMethods.contains(DistributionMethod.STREAM)) {
            includeVideosInSearchForStream.invoke(videoIds = videoIds)
        } else {
            excludeVideosFromSearchForStream.invoke(videoIds = videoIds)
        }
    }
}
