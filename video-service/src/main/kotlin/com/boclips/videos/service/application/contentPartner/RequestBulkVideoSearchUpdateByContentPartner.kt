package com.boclips.videos.service.application.contentPartner

import com.boclips.events.config.Topics
import com.boclips.videos.service.application.exceptions.ContentPartnerNotFoundException
import com.boclips.videos.service.application.video.search.ExcludeVideosFromSearchForDownload
import com.boclips.videos.service.application.video.search.ExcludeVideosFromSearchForStream
import com.boclips.videos.service.application.video.search.IncludeVideosInSearchForDownload
import com.boclips.videos.service.application.video.search.IncludeVideosInSearchForStream
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerId
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerRepository
import com.boclips.videos.service.domain.model.video.DeliveryMethod
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
    fun invoke(contentPartnerId: ContentPartnerId, deliveryMethods: Set<DeliveryMethod>) {
        if (contentPartnerRepository.findById(contentPartnerId) == null) {
            throw ContentPartnerNotFoundException("Cannot find Content Partner with id: ${contentPartnerId.value}")
        }

        val videoIds = videoRepository.findByContentPartnerId(contentPartnerId)
            .map { it.videoId.value }

        videoIds.windowed(size = batchSize, step = batchSize, partialWindows = true)
            .forEach { this.publish(deliveryMethods, it) }
    }

    private fun publish(deliveryMethods: Set<DeliveryMethod>, videoIds: List<String>) {
        if (deliveryMethods.contains(DeliveryMethod.STREAM)) {
            excludeVideosFromSearchForStream.invoke(videoIds)
        } else {
            includeVideosInSearchForStream.invoke(videoIds)
        }

        if (deliveryMethods.contains(DeliveryMethod.DOWNLOAD)) {
            excludeVideosFromSearchForDownload.invoke(videoIds)
        } else {
            includeVideosInSearchForDownload.invoke(videoIds)
        }
    }
}
