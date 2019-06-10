package com.boclips.videos.service.application.contentPartner

import com.boclips.videos.service.domain.model.ageRange.UnboundedAgeRange
import com.boclips.videos.service.domain.model.contentPartner.ContentPartner
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerId
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerRepository
import mu.KLogging
import org.bson.types.ObjectId

class CreateOrFindContentPartner(
    val contentPartnerRepository: ContentPartnerRepository
) {
    companion object : KLogging()

    operator fun invoke(provider: String): ContentPartner {
        val existingContentPartner = contentPartnerRepository.findByName(provider)

        if (existingContentPartner == null) {
            logger.info { "Create new content partner $provider" }
            val contentPartner = ContentPartner(
                contentPartnerId = ContentPartnerId(value = ObjectId().toHexString()),
                name = provider,
                ageRange = UnboundedAgeRange
            )

            return contentPartnerRepository.create(contentPartner)
        }

        return existingContentPartner
    }
}