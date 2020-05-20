package com.boclips.contentpartner.service.presentation.converters

import com.boclips.contentpartner.service.domain.model.channel.Channel
import com.boclips.contentpartner.service.domain.model.channel.ContentType
import com.boclips.contentpartner.service.domain.model.channel.Credit
import com.boclips.contentpartner.service.domain.model.channel.PedagogyInformation
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
    fun convert(channel: Channel): ContentPartnerResource {
        return ContentPartnerResource(
            id = channel.id.value,
            name = channel.name,
            official = when (channel.credit) {
                is Credit.PartnerCredit -> true
                is Credit.YoutubeCredit -> false
            },
            legalRestriction = channel.legalRestriction?.let {
                legalRestrictionsToResourceConverter.convert(it)
            },
            distributionMethods = DistributionMethodResourceConverter.toDeliveryMethodResources(
                channel.distributionMethods
            ),
            description = channel.description,
            currency = channel.currency?.currencyCode,
            contentCategories = channel.contentCategories?.map { toContentCategoryResource(it) },
            hubspotId = channel.hubspotId,
            awards = channel.awards,
            notes = channel.notes,
            ingest = ingestDetailsToResourceConverter.convert(channel.ingest),
            deliveryFrequency = channel.deliveryFrequency,
            language = channel.language?.let { it -> toLanguageResource(it) },
            contentTypes = channel.contentTypes?.map {
                when (it) {
                    ContentType.INSTRUCTIONAL -> ContentTypeResource.INSTRUCTIONAL
                    ContentType.NEWS -> ContentTypeResource.NEWS
                    ContentType.STOCK -> ContentTypeResource.STOCK
                }
            },
            oneLineDescription = channel.marketingInformation?.oneLineDescription,
            marketingInformation = MarketingInformationToResourceConverter.from(
                channel.marketingInformation
            ),
            pedagogyInformation = PedagogyInformationToResourceConverter.from(
                pedagogyInformation = PedagogyInformation(
                    isTranscriptProvided = channel.pedagogyInformation?.isTranscriptProvided,
                    educationalResources = channel.pedagogyInformation?.educationalResources,
                    curriculumAligned = channel.pedagogyInformation?.curriculumAligned,
                    bestForTags = channel.pedagogyInformation?.bestForTags,
                    subjects = channel.pedagogyInformation?.subjects,
                    ageRangeBuckets = channel.pedagogyInformation?.ageRangeBuckets
                )
            ),
            contractId = channel.contract?.id?.value,
            contractName = channel.contract?.contentPartnerName,
            _links = listOf(contentPartnersLinkBuilder.self(channel.id.value))
                .map { it.rel to it }
                .toMap()
        )
    }
}
