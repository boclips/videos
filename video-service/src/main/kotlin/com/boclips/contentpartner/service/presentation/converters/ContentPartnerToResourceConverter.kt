package com.boclips.contentpartner.service.presentation.converters

import com.boclips.contentpartner.service.domain.model.contentpartner.ContentPartner
import com.boclips.contentpartner.service.domain.model.contentpartner.ContentPartnerType
import com.boclips.contentpartner.service.domain.model.contentpartner.Credit
import com.boclips.contentpartner.service.domain.model.contentpartner.PedagogyInformation
import com.boclips.contentpartner.service.presentation.hateoas.ContentPartnersLinkBuilder
import com.boclips.videos.api.response.contentpartner.ContentPartnerResource
import com.boclips.videos.api.response.contentpartner.ContentTypeResource
import com.boclips.videos.api.response.contentpartner.toLanguageResource
import com.boclips.videos.service.domain.model.video.toContentCategoryResource

class ContentPartnerToResourceConverter(
    private val contentPartnersLinkBuilder: ContentPartnersLinkBuilder,
    private val ingestDetailsToResourceConverter: IngestDetailsResourceConverter,
    private val legalRestrictionsToResourceConverter: LegalRestrictionsToResourceConverter
) {
    fun convert(contentPartner: ContentPartner): ContentPartnerResource {
        return ContentPartnerResource(
            id = contentPartner.contentPartnerId.value,
            name = contentPartner.name,
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
            currency = contentPartner.currency?.currencyCode,
            contentCategories = contentPartner.contentCategories?.map { toContentCategoryResource(it) },
            hubspotId = contentPartner.hubspotId,
            awards = contentPartner.awards,
            notes = contentPartner.notes,
            ingest = ingestDetailsToResourceConverter.convert(contentPartner.ingest),
            deliveryFrequency = contentPartner.deliveryFrequency,
            language = contentPartner.language?.let { it -> toLanguageResource(it) },
            contentTypes = contentPartner.contentTypes?.map {
                when (it) {
                    ContentPartnerType.INSTRUCTIONAL -> ContentTypeResource.INSTRUCTIONAL
                    ContentPartnerType.NEWS -> ContentTypeResource.NEWS
                    ContentPartnerType.STOCK -> ContentTypeResource.STOCK
                }
            },
            oneLineDescription = contentPartner.marketingInformation?.oneLineDescription,
            marketingInformation = MarketingInformationToResourceConverter.from(
                contentPartner.marketingInformation
            ),
            pedagogyInformation = PedagogyInformationToResourceConverter.from(
                pedagogyInformation = PedagogyInformation(
                    isTranscriptProvided = contentPartner.pedagogyInformation?.isTranscriptProvided,
                    educationalResources = contentPartner.pedagogyInformation?.educationalResources,
                    curriculumAligned = contentPartner.pedagogyInformation?.curriculumAligned,
                    bestForTags = contentPartner.pedagogyInformation?.bestForTags,
                    subjects = contentPartner.pedagogyInformation?.subjects,
                    ageRangeBuckets = contentPartner.pedagogyInformation?.ageRangeBuckets
                )
            ),
            contractId = contentPartner.contract?.id?.value,
            contractName = contentPartner.contract?.contentPartnerName,
            _links = listOf(contentPartnersLinkBuilder.self(contentPartner.contentPartnerId.value))
                .map { it.rel to it }
                .toMap()
        )
    }
}
