package com.boclips.videos.api.response.channel

import ChannelTaxonomyResource
import com.boclips.videos.api.BoclipsInternalProjection
import com.boclips.videos.api.PublicApiProjection
import com.boclips.videos.api.response.HateoasLink
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonView
import java.time.Period

data class ChannelResource(
    @get:JsonView(PublicApiProjection::class)
    val id: String,
    @get:JsonView(PublicApiProjection::class)
    val name: String,
    @get:JsonView(PublicApiProjection::class)
    val legalRestriction: LegalRestrictionResource? = null,
    @get:JsonView(PublicApiProjection::class)
    val description: String? = null,
    @get:JsonView(PublicApiProjection::class)
    val contentCategories: List<ContentCategoryResource>? = null,
    @get:JsonView(PublicApiProjection::class)
    val language: LanguageResource? = null,
    @get:JsonView(PublicApiProjection::class)
    val notes: String? = null,
    @get:JsonView(PublicApiProjection::class)
    val contentTypes: List<ContentTypeResource>? = emptyList(),
    @get:JsonView(PublicApiProjection::class)
    val oneLineDescription: String? = null,

    @get:JsonView(BoclipsInternalProjection::class)
    val currency: String? = null,
    @get:JsonView(BoclipsInternalProjection::class)
    val official: Boolean? = null,
    @get:JsonView(BoclipsInternalProjection::class)
    val distributionMethods: Set<DistributionMethodResource>? = null,
    @get:JsonView(BoclipsInternalProjection::class)
    val marketingInformation: MarketingResource? = null,
    @get:JsonView(BoclipsInternalProjection::class)
    val pedagogyInformation: PedagogyResource? = null,
    @get:JsonView(BoclipsInternalProjection::class)
    val ingest: IngestDetailsResource? = null,

    @get:JsonView(BoclipsInternalProjection::class)
    val contractId: String? = null,

    @get:JsonView(BoclipsInternalProjection::class)
    val contractName: String? = null,

    @get:JsonView(BoclipsInternalProjection::class)
    val taxonomy: ChannelTaxonomyResource? = null,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    var _links: Map<String, HateoasLink>? = null
)
