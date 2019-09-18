package com.boclips.contentpartner.service.application

import com.boclips.contentpartner.service.domain.model.ContentPartnerRepository
import com.boclips.contentpartner.service.presentation.ContentPartnerResource
import com.boclips.contentpartner.service.presentation.ContentPartnerToResourceConverter
import com.boclips.contentpartner.service.presentation.ContentPartnersLinkBuilder
import org.springframework.hateoas.Resource
import org.springframework.hateoas.Resources

class GetContentPartners(
    private val contentPartnerRepository: ContentPartnerRepository,
    private val contentPartnersLinkBuilder: ContentPartnersLinkBuilder
) {
    operator fun invoke(
        name: String? = null,
        official: Boolean? = null,
        accreditedToYtChannelId: String? = null
    ): Resources<Resource<ContentPartnerResource>> {
        val filters = ContentPartnerFiltersConverter.convert(
            name,
            official,
            accreditedToYtChannelId
        )

        val contentPartners = contentPartnerRepository.findAll(filters)
            .map {
                Resource(
                    ContentPartnerToResourceConverter.convert(it),
                    contentPartnersLinkBuilder.self(it)
                )
            }

        return Resources(contentPartners)
    }
}
