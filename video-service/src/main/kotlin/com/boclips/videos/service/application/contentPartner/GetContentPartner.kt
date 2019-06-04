package com.boclips.videos.service.application.contentPartner

import com.boclips.videos.service.application.exceptions.NonNullableFieldCreateRequestException.Companion.getOrThrow
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerId
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerRepository
import com.boclips.videos.service.presentation.contentPartner.ContentPartnerResource
import com.boclips.videos.service.presentation.contentPartner.ContentPartnerToResourceConverter
import com.boclips.web.exceptions.ResourceNotFoundApiException

class GetContentPartner(
    private val contentPartnerRepository: ContentPartnerRepository
) {
    operator fun invoke(contentPartnerId: String?): ContentPartnerResource {
        val id = getOrThrow(contentPartnerId, "contentPartnerId")

        val contentPartner = contentPartnerRepository.findById(ContentPartnerId(id))
            ?: throw ResourceNotFoundApiException(
                error = "Content partner not found",
                message = "No content partner found for this id: $contentPartnerId"
            )

        return ContentPartnerToResourceConverter.convert(contentPartner)
    }
}