package com.boclips.contentpartner.service.application

import com.boclips.contentpartner.service.domain.model.ContentPartnerId
import com.boclips.contentpartner.service.domain.model.ContentPartnerRepository
import com.boclips.contentpartner.service.domain.model.User
import com.boclips.contentpartner.service.presentation.ContentPartnerResource
import com.boclips.contentpartner.service.presentation.ContentPartnerToResourceConverter
import com.boclips.web.exceptions.ResourceNotFoundApiException

class GetContentPartner(private val contentPartnerRepository: ContentPartnerRepository) {
    operator fun invoke(contentPartnerId: String, user: User): ContentPartnerResource {
        val contentPartner = contentPartnerRepository.findById(
            ContentPartnerId(value = contentPartnerId)
        )
            ?: throw ResourceNotFoundApiException(
                error = "Content partner not found",
                message = "No content partner found for this id: $contentPartnerId"
            )

        return ContentPartnerToResourceConverter.convert(contentPartner, user)
    }
}
