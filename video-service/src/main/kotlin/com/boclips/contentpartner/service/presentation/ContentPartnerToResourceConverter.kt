package com.boclips.contentpartner.service.presentation

import com.boclips.contentpartner.service.domain.model.ContentPartner
import com.boclips.contentpartner.service.domain.model.Credit
import com.boclips.contentpartner.service.presentation.ageRange.AgeRangeToResourceConverter
import com.boclips.contentpartner.service.presentation.hateoas.ContentPartnersLinkBuilder
import com.boclips.videos.api.response.contentpartner.ContentPartnerResource
import com.boclips.videos.api.response.contentpartner.toLanguageResource
import com.boclips.videos.service.domain.model.video.toContentCategoryResource

class ContentPartnerToResourceConverter(
    private val contentPartnersLinkBuilder: ContentPartnersLinkBuilder,
    private val legalRestrictionsToResourceConverter: LegalRestrictionsToResourceConverter
) {
    fun convert(contentPartner: ContentPartner): ContentPartnerResource {
        return ContentPartnerResource(
            id = contentPartner.contentPartnerId.value,
            name = contentPartner.name,
            ageRange = AgeRangeToResourceConverter.convert(contentPartner.ageRangeBuckets),
            official = when (contentPartner.credit) {
                is Credit.PartnerCredit -> true
                is Credit.YoutubeCredit -> false
            },
            legalRestriction = contentPartner.legalRestriction?.let {
                legalRestrictionsToResourceConverter.convert(it)
            },
            distributionMethods = DistributionMethodResourceConverter.toDeliveryMethodResources(
                contentPartner.distributionMethods
            ),
            description = contentPartner.description,
            currency = contentPartner.remittance?.currency?.currencyCode,
            contentCategories = contentPartner.contentCategories?.map { toContentCategoryResource(it) },
            hubspotId = contentPartner.hubspotId,
            awards = contentPartner.awards,
            notes = contentPartner.notes,
            language = contentPartner.language?.let { it -> toLanguageResource(it) },
            contentTypes = contentPartner.contentTypes?.map { it.name },
            oneLineDescription = contentPartner.marketingInformation?.oneLineDescription,
            marketingInformation = MarketingInformationToResourceConverter
                .from(contentPartner.marketingInformation),
            isTranscriptProvided = contentPartner.isTranscriptProvided,
            educationalResources = contentPartner.educationalResources,
            _links = listOf(contentPartnersLinkBuilder.self(contentPartner.contentPartnerId.value))
                .map { it.rel to it }
                .toMap()
        )
    }
}
