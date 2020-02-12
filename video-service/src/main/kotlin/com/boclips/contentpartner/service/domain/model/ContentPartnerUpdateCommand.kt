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

    class ReplaceContentTypes(contentPartnerId: ContentPartnerId, val contentType: List<String>) :
        ContentPartnerUpdateCommand(contentPartnerId)

    class ReplaceContentCategories(contentPartnerId: ContentPartnerId, val contentCategories: List<String>) :
        ContentPartnerUpdateCommand(contentPartnerId)

    class ReplaceLanguage(contentPartnerId: ContentPartnerId, val language: String) :
        ContentPartnerUpdateCommand(contentPartnerId)

    class ReplaceDescription(contentPartnerId: ContentPartnerId, val description: String) :
        ContentPartnerUpdateCommand(contentPartnerId)

    class ReplaceAwards(contentPartnerId: ContentPartnerId, val awards: String) :
        ContentPartnerUpdateCommand(contentPartnerId)

    class ReplaceHubspotId(contentPartnerId: ContentPartnerId, val hubspotId: String) :
        ContentPartnerUpdateCommand(contentPartnerId)

    class ReplaceNotes(contentPartnerId: ContentPartnerId, val notes: String) :
        ContentPartnerUpdateCommand(contentPartnerId)

    class ReplaceMarketingStatus(contentPartnerId: ContentPartnerId, val status: ContentPartnerStatus) :
        ContentPartnerUpdateCommand(contentPartnerId)

    class ReplaceOneLineDescription(contentPartnerId: ContentPartnerId, val oneLineDescription: String) :
        ContentPartnerUpdateCommand(contentPartnerId)
}