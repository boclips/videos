package com.boclips.videos.service.application.contentPartner

import com.boclips.videos.service.application.exceptions.ContentPartnerNotFoundException
import com.boclips.videos.service.application.video.search.ExcludeVideosFromSearchForDownload
import com.boclips.videos.service.application.video.search.ExcludeVideosFromSearchForStream
import com.boclips.videos.service.application.video.search.IncludeVideosInSearchForDownload
import com.boclips.videos.service.application.video.search.IncludeVideosInSearchForStream
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerId
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerRepository
import com.boclips.videos.service.domain.model.video.DistributionMethod
import com.boclips.videos.service.domain.model.video.VideoRepository

class RequestBulkVideoSearchUpdateByContentPartner(
    private val contentPartnerRepository: ContentPartnerRepository,
    private val videoRepository: VideoRepository,
    private val batchSize: Int,
    private val includeVideosInSearchForStream: IncludeVideosInSearchForStream,
    private val excludeVideosFromSearchForStream: ExcludeVideosFromSearchForStream,
    private val includeVideosInSearchForDownload: IncludeVideosInSearchForDownload,
    private val excludeVideosFromSearchForDownload: ExcludeVideosFromSearchForDownload
) {
    fun invoke(contentPartnerId: ContentPartnerId, distributionMethods: Set<DistributionMethod>) {
        if (contentPartnerRepository.findById(contentPartnerId) == null) {
            throw ContentPartnerNotFoundException("Cannot find Content Partner with id: ${contentPartnerId.value}")
        }

        val videoIds = videoRepository.findByContentPartnerId(contentPartnerId)
            .map { it.videoId.value }

        videoIds.windowed(size = batchSize, step = batchSize, partialWindows = true)
            .forEach { this.publish(distributionMethods, it) }
    }

    private fun publish(distributionMethods: Set<DistributionMethod>, videoIds: List<String>) {
        if (distributionMethods.contains(DistributionMethod.STREAM)) {
            includeVideosInSearchForStream.invoke(videoIds)
        } else {
            excludeVideosFromSearchForStream.invoke(videoIds)
        }

        if (distributionMethods.contains(DistributionMethod.DOWNLOAD)) {
            includeVideosInSearchForDownload.invoke(videoIds)
        } else {
            excludeVideosFromSearchForDownload.invoke(videoIds)
        }
    }
}
