package com.boclips.contentpartner.service.presentation.converters

import com.boclips.contentpartner.service.application.channel.ContentCategoryConverter
import com.boclips.contentpartner.service.domain.model.channel.Channel
import com.boclips.contentpartner.service.domain.model.channel.ContentType
import com.boclips.contentpartner.service.domain.model.channel.PedagogyInformation
import com.boclips.contentpartner.service.domain.model.channel.Taxonomy
import com.boclips.contentpartner.service.presentation.hateoas.ChannelLinkBuilder
import com.boclips.videos.api.request.Projection
import com.boclips.videos.api.response.channel.ChannelResource
import com.boclips.videos.api.response.channel.ContentTypeResource
import com.boclips.videos.api.response.channel.TaxonomyCategoryResource
import com.boclips.videos.api.response.channel.toLanguageResource
import com.boclips.videos.service.domain.model.taxonomy.Category

class ChannelToResourceConverter(
    private val channelLinkBuilder: ChannelLinkBuilder,
    private val ingestDetailsToResourceConverter: IngestDetailsResourceConverter,
    private val legalRestrictionsToResourceConverter: LegalRestrictionsToResourceConverter
) {
    fun convert(channel: Channel, projection: Projection? = Projection.full): ChannelResource {
        return when (projection) {
            Projection.list -> ChannelResource(
                id = channel.id.value,
                name = channel.name,
                contentTypes = null
            )
            else -> ChannelResource(
                id = channel.id.value,
                name = channel.name,
                legalRestriction = channel.legalRestriction?.let {
                    legalRestrictionsToResourceConverter.convert(it)
                },
                distributionMethods = DistributionMethodResourceConverter.toDeliveryMethodResources(
                    channel.distributionMethods
                ),
                description = channel.description,
                currency = channel.currency?.currencyCode,
                contentCategories = channel.contentCategories?.let { ContentCategoryConverter.convertToResource(it) },
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
                        bestForTags = channel.pedagogyInformation?.bestForTags,
                        subjects = channel.pedagogyInformation?.subjects,
                        ageRangeBuckets = channel.pedagogyInformation?.ageRangeBuckets
                    )
                ),
                contractId = channel.contract?.id?.value,
                contractName = channel.contract?.contentPartnerName,
                taxonomy = TaxonomyResourceConverter.toResource(channel.taxonomy),
                _links = listOf(channelLinkBuilder.self(channel.id.value))
                    .map { it.rel to it }
                    .toMap()
            )
        }
    }
}
