package com.boclips.videos.service.application.contentPartner

import com.boclips.events.config.Topics
import com.boclips.events.types.ContentPartnerExclusionFromSearchRequested
import com.boclips.events.types.ContentPartnerInclusionInSearchRequested
import com.boclips.videos.service.application.exceptions.ContentPartnerNotFoundException
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerId
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerRepository
import org.springframework.messaging.support.MessageBuilder

class RequestSearchUpdateByContentPartner(
    private val topics: Topics,
    private val contentPartnerRepository: ContentPartnerRepository
) {

    enum class RequestType {
        INCLUDE,
        EXCLUDE
    }

    fun invoke(contentPartnerId: ContentPartnerId, requestType: RequestType) {
        if (contentPartnerRepository.findById(contentPartnerId) == null) {
            throw ContentPartnerNotFoundException("Cannot find Content Partner with id: ${contentPartnerId.value}")
        }

        when (requestType) {
            RequestType.INCLUDE -> publishInclusion(contentPartnerId)
            RequestType.EXCLUDE -> publishExclusion(contentPartnerId)
        }
    }

    private fun publishInclusion(contentPartnerId: ContentPartnerId) {
        val message = ContentPartnerInclusionInSearchRequested
            .builder()
            .contentPartnerId(contentPartnerId.value)
            .build()
        topics.contentPartnerInclusionInSearchRequested()
            .send(MessageBuilder.withPayload(message).build())
    }

    private fun publishExclusion(contentPartnerId: ContentPartnerId) {
        val message = ContentPartnerExclusionFromSearchRequested
            .builder()
            .contentPartnerId(contentPartnerId.value)
            .build()
        topics.contentPartnerExclusionFromSearchRequested()
            .send(MessageBuilder.withPayload(message).build())
    }
}
