package com.boclips.videos.service.presentation.contentPartner

import com.boclips.videos.service.domain.model.contentPartner.ContentPartner
import com.boclips.videos.service.presentation.ageRange.AgeRangeToResourceConverter

object ContentPartnerToResourceConverter {
    fun convert(contentPartner: ContentPartner): ContentPartnerResource {
        return ContentPartnerResource(
            name = contentPartner.name,
            ageRange = AgeRangeToResourceConverter.convert(contentPartner.ageRange)
        )
    }
}