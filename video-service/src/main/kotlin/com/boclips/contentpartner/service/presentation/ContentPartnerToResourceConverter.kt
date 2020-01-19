package com.boclips.contentpartner.service.presentation

import com.boclips.contentpartner.service.domain.model.ContentPartner
import com.boclips.contentpartner.service.domain.model.Credit
import com.boclips.contentpartner.service.domain.model.User
import com.boclips.contentpartner.service.presentation.ageRange.AgeRangeToResourceConverter
import com.boclips.videos.api.response.contentpartner.ContentPartnerResource

class ContentPartnerToResourceConverter(private val contentPartnersLinkBuilder: ContentPartnersLinkBuilder) {
    fun convert(contentPartner: ContentPartner, user: User): ContentPartnerResource {
        return ContentPartnerResource(
            id = contentPartner.contentPartnerId.value,
            name = contentPartner.name,
            ageRange = AgeRangeToResourceConverter.convert(contentPartner.ageRange),
            official = when (contentPartner.credit) {
                is Credit.PartnerCredit -> true
                is Credit.YoutubeCredit -> false
            },
            legalRestrictions = contentPartner.legalRestrictions?.let {
                LegalRestrictionsToResourceConverter().convert(
                    it
                )
            },
            distributionMethods = DistributionMethodResourceConverter.toDeliveryMethodResources(
                contentPartner.distributionMethods
            ),
            currency = if (user.isPermittedToAccessBackoffice)
                contentPartner.remittance?.currency?.currencyCode else null,
            _links = listOf(contentPartnersLinkBuilder.self(contentPartner.contentPartnerId.value)).map { it.rel.value() to it }.toMap()
        )
    }
}
