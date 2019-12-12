package com.boclips.contentpartner.service.presentation

import com.boclips.contentpartner.service.application.LegalRestrictionsResource
import com.boclips.contentpartner.service.domain.model.ContentPartner
import com.boclips.contentpartner.service.domain.model.Credit
import com.boclips.videos.service.domain.model.User
import com.boclips.videos.service.presentation.ageRange.AgeRangeToResourceConverter
import com.boclips.videos.service.presentation.deliveryMethod.DistributionMethodResourceConverter

object ContentPartnerToResourceConverter {
    fun convert(contentPartner: ContentPartner, user: User): ContentPartnerResource {
        return ContentPartnerResource(
            id = contentPartner.contentPartnerId.value,
            name = contentPartner.name,
            ageRange = AgeRangeToResourceConverter.convert(contentPartner.ageRange),
            official = when (contentPartner.credit) {
                is Credit.PartnerCredit -> true
                is Credit.YoutubeCredit -> false
            },
            legalRestrictions = contentPartner.legalRestrictions?.let { LegalRestrictionsResource.from(it) },
            distributionMethods = DistributionMethodResourceConverter.toDeliveryMethodResources(
                contentPartner.distributionMethods
            ),
            currency = if (user.isAdministrator)
                contentPartner.remittance?.currency?.currencyCode else null
        )
    }
}
