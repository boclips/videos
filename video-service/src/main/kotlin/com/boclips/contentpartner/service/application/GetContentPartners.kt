package com.boclips.contentpartner.service.application

import com.boclips.contentpartner.service.domain.model.ContentPartner
import com.boclips.contentpartner.service.domain.model.ContentPartnerRepository

class GetContentPartners(private val contentPartnerRepository: ContentPartnerRepository) {
    operator fun invoke(
        name: String? = null,
        official: Boolean? = null,
        accreditedToYtChannelId: String? = null,
        ingestTypes: List<String>? = null
    ): Iterable<ContentPartner> {
        val filters = ContentPartnerFiltersConverter.convert(
            name = name,
            official = official,
            accreditedYTChannelId = accreditedToYtChannelId,
            ingestTypes = ingestTypes
        )

        return contentPartnerRepository.findAll(filters)
    }
}
