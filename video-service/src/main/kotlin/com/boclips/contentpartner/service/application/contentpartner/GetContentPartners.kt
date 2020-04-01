package com.boclips.contentpartner.service.application.contentpartner

import com.boclips.contentpartner.service.domain.model.contentpartner.ContentPartner
import com.boclips.contentpartner.service.domain.model.contentpartner.ContentPartnerRepository
import com.boclips.videos.api.common.IngestType

class GetContentPartners(private val contentPartnerRepository: ContentPartnerRepository) {
    operator fun invoke(
        name: String? = null,
        official: Boolean? = null,
        accreditedToYtChannelId: String? = null,
        ingestTypes: List<IngestType>? = null
    ): Iterable<ContentPartner> {
        val filters =
            ContentPartnerFiltersConverter.convert(
                name = name,
                official = official,
                accreditedYTChannelId = accreditedToYtChannelId,
                ingestTypes = ingestTypes
            )

        return contentPartnerRepository.findAll(filters)
    }
}
