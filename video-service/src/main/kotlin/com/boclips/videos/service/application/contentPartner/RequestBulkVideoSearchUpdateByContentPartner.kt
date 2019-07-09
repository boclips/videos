package com.boclips.videos.service.application.contentPartner

import com.boclips.events.config.Topics
import com.boclips.events.types.video.VideosExclusionFromDownloadRequested
import com.boclips.events.types.video.VideosExclusionFromSearchRequested
import com.boclips.events.types.video.VideosExclusionFromStreamRequested
import com.boclips.events.types.video.VideosInclusionInDownloadRequested
import com.boclips.events.types.video.VideosInclusionInSearchRequested
import com.boclips.events.types.video.VideosInclusionInStreamRequested
import com.boclips.videos.service.application.exceptions.ContentPartnerNotFoundException
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerId
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerRepository
import com.boclips.videos.service.domain.model.video.DeliveryMethod
import com.boclips.videos.service.domain.model.video.VideoRepository
import org.springframework.integration.support.MessageBuilder

class RequestBulkVideoSearchUpdateByContentPartner(
    private val topics: Topics,
    private val contentPartnerRepository: ContentPartnerRepository,
    private val videoRepository: VideoRepository,
    private val batchSize: Int
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
            excludeFromStream(videoIds)
        } else {
            includeInStream(videoIds)
        }

        if (deliveryMethods.contains(DeliveryMethod.DOWNLOAD)) {
            excludeFromDownload(videoIds)
        } else {
            includeInDownload(videoIds)
        }
    }

    private fun includeInStream(videoIds: List<String>) {
        val message = VideosInclusionInStreamRequested.builder().videoIds(videoIds).build()
        topics.videosInclusionInStreamRequested().send(MessageBuilder.withPayload(message).build())
    }

    private fun excludeFromStream(videoIds: List<String>) {
        val message = VideosExclusionFromStreamRequested.builder().videoIds(videoIds).build()
        topics.videosExclusionFromStreamRequested().send(MessageBuilder.withPayload(message).build())
    }

    private fun includeInDownload(videoIds: List<String>) {
        val message = VideosInclusionInDownloadRequested.builder().videoIds(videoIds).build()
        topics.videosInclusionInDownloadRequested().send(MessageBuilder.withPayload(message).build())
    }

    private fun excludeFromDownload(videoIds: List<String>) {
        val message = VideosExclusionFromDownloadRequested.builder().videoIds(videoIds).build()
        topics.videosExclusionFromDownloadRequested().send(MessageBuilder.withPayload(message).build())
    }
}
