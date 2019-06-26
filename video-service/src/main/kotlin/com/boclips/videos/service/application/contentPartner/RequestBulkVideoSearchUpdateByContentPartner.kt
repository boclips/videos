package com.boclips.videos.service.application.contentPartner

import com.boclips.events.config.Topics
import com.boclips.events.types.VideosExclusionFromSearchRequested
import com.boclips.events.types.VideosInclusionInSearchRequested
import com.boclips.videos.service.application.exceptions.ContentPartnerNotFoundException
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerId
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerRepository
import com.boclips.videos.service.domain.model.video.VideoRepository
import org.springframework.integration.support.MessageBuilder

class RequestBulkVideoSearchUpdateByContentPartner(
    private val topics: Topics,
    private val contentPartnerRepository: ContentPartnerRepository,
    private val videoRepository: VideoRepository,
    private val batchSize: Int
) {

    enum class RequestType {
        INCLUDE,
        EXCLUDE
    }

    fun invoke(contentPartnerId: ContentPartnerId, requestType: RequestType) {
        if (contentPartnerRepository.findById(contentPartnerId) == null) {
            throw ContentPartnerNotFoundException("Cannot find Content Partner with id: ${contentPartnerId.value}")
        }

        val videoIds = videoRepository.findByContentPartnerId(contentPartnerId)
            .map { it.videoId.value }

        videoIds.windowed(size = batchSize, step = batchSize, partialWindows = true)
            .forEach { this.publish(requestType, it) }
    }

    private fun publish(requestType: RequestType, videoIds: List<String>) {
        when (requestType) {
            RequestType.INCLUDE -> publishInclusion(videoIds)
            RequestType.EXCLUDE -> publishExclusion(videoIds)
        }
    }

    private fun publishInclusion(videoIds: List<String>) {
        val message = VideosInclusionInSearchRequested
            .builder()
            .videoIds(videoIds)
            .build()

        topics.videosInclusionInSearchRequested()
            .send(MessageBuilder.withPayload(message).build())
    }

    private fun publishExclusion(videoIds: List<String>) {
        val message = VideosExclusionFromSearchRequested
            .builder()
            .videoIds(videoIds)
            .build()
        topics.videosExclusionFromSearchRequested()
            .send(MessageBuilder.withPayload(message).build())
    }
}
