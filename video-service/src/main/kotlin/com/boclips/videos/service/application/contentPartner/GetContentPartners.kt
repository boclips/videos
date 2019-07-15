package com.boclips.videos.service.application.contentPartner

import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerRepository
import com.boclips.videos.service.presentation.contentPartner.ContentPartnerResource
import com.boclips.videos.service.presentation.contentPartner.ContentPartnerToResourceConverter
import com.boclips.videos.service.presentation.hateoas.ContentPartnersLinkBuilder
import org.springframework.hateoas.Resource
import org.springframework.hateoas.Resources

class GetContentPartners(
    private val contentPartnerRepository: ContentPartnerRepository,
    private val contentPartnersLinkBuilder: ContentPartnersLinkBuilder
) {
    operator fun invoke(
        name: String? = null,
        isOfficial: Boolean? = null,
        accreditedToYtChannelId: String? = null
    ): Resources<Resource<ContentPartnerResource>> {
        val filters = ContentPartnerFiltersConverter.convert(name, isOfficial, accreditedToYtChannelId)

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