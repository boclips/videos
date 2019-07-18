package com.boclips.videos.service.presentation.contentPartner

import com.boclips.videos.service.domain.model.contentPartner.ContentPartner
import com.boclips.videos.service.domain.model.contentPartner.Credit
import com.boclips.videos.service.presentation.ageRange.AgeRangeToResourceConverter
import com.boclips.videos.service.presentation.deliveryMethod.DistributionMethodResourceConverter

object ContentPartnerToResourceConverter {
    fun convert(contentPartner: ContentPartner): ContentPartnerResource {
        return ContentPartnerResource(
            id = contentPartner.contentPartnerId.value,
            name = contentPartner.name,
            ageRange = AgeRangeToResourceConverter.convert(contentPartner.ageRange),
            official = when (contentPartner.credit) {
                is Credit.PartnerCredit -> true
                is Credit.YoutubeCredit -> false
            },
            distributionMethods = DistributionMethodResourceConverter.toDeliveryMethodResources(
                contentPartner.distributionMethods
            )
        )
    }
}
