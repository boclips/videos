package com.boclips.videos.service.application.contentPartner

import com.boclips.videos.service.domain.model.ageRange.UnboundedAgeRange
import com.boclips.videos.service.domain.model.contentPartner.ContentPartner
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerId
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerRepository
import mu.KLogging

class CreateOrUpdateContentPartner(val contentPartnerRepository: ContentPartnerRepository) {
    companion object : KLogging()

    operator fun invoke(contentPartnerId: ContentPartnerId, provider: String): ContentPartner {
        val existingContentPartner = contentPartnerRepository.findById(contentPartnerId)

        if (existingContentPartner == null) {
            logger.info { "Create new content partner $provider" }

            val contentPartner = ContentPartner(
                contentPartnerId = contentPartnerId,
                name = provider,
                ageRange = UnboundedAgeRange
            )

            return contentPartnerRepository.create(contentPartner)
        }

        val contentPartnerToBeUpdated = existingContentPartner.copy(name = provider)

        return contentPartnerRepository.update(contentPartnerToBeUpdated)
    }
}