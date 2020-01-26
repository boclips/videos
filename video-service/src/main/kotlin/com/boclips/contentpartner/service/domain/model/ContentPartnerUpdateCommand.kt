package com.boclips.contentpartner.service.domain.model

import java.util.Currency

sealed class ContentPartnerUpdateCommand(val contentPartnerId: ContentPartnerId) {

    class ReplaceName(contentPartnerId: ContentPartnerId, val name: String) :
        ContentPartnerUpdateCommand(contentPartnerId)

    class ReplaceAgeRange(contentPartnerId: ContentPartnerId, val ageRange: AgeRange) :
        ContentPartnerUpdateCommand(contentPartnerId)

    class ReplaceDistributionMethods(
        contentPartnerId: ContentPartnerId, val distributionMethods: Set<DistributionMethod>
    ) : ContentPartnerUpdateCommand(contentPartnerId)

    class ReplaceLegalRestrictions(contentPartnerId: ContentPartnerId, val legalRestriction: LegalRestriction) :
        ContentPartnerUpdateCommand(contentPartnerId)

    class ReplaceCurrency(contentPartnerId: ContentPartnerId, val currency: Currency) :
        ContentPartnerUpdateCommand(contentPartnerId)
}
