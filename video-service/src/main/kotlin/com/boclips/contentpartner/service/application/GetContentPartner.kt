package com.boclips.contentpartner.service.application

import com.boclips.contentpartner.service.domain.model.ContentPartner
import com.boclips.contentpartner.service.domain.model.ContentPartnerId
import com.boclips.contentpartner.service.domain.model.ContentPartnerRepository
import com.boclips.web.exceptions.ResourceNotFoundApiException

class GetContentPartner(
    private val contentPartnerRepository: ContentPartnerRepository
) {
    operator fun invoke(contentPartnerId: String): ContentPartner {
        return contentPartnerRepository.findById(
            ContentPartnerId(value = contentPartnerId)
        )
            ?: throw ResourceNotFoundApiException(
                error = "Content partner not found",
                message = "No content partner found for this id: $contentPartnerId"
            )
    }
}
