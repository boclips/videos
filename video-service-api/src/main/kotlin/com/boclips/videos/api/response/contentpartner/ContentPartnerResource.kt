package com.boclips.videos.api.response.contentpartner

import com.boclips.videos.api.BoclipsInternalProjection
import com.boclips.videos.api.PublicApiProjection
import com.boclips.videos.api.response.HateoasLink
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonView

data class ContentPartnerResource(
    @get:JsonView(PublicApiProjection::class)
    val id: String,
    @get:JsonView(PublicApiProjection::class)
    val name: String,
    @get:JsonView(PublicApiProjection::class)
    val ageRange: AgeRangeResource? = null,
    @get:JsonView(PublicApiProjection::class)
    val legalRestriction: LegalRestrictionResource? = null,
    @get:JsonView(PublicApiProjection::class)
    val description: String? = null,
    @get:JsonView(PublicApiProjection::class)
    val contentCategories: List<ContentCategoryResource>? = null,
    @get:JsonView(PublicApiProjection::class)
    val language: LanguageResource? = null,
    @get:JsonView(PublicApiProjection::class)
    val awards: String? = null,
    @get:JsonView(PublicApiProjection::class)
    val notes: String? = null,
    @get:JsonView(PublicApiProjection::class)
    val contentTypes: List<String>? = emptyList(),

    @get:JsonView(BoclipsInternalProjection::class)
    val hubspotId: String? = null,
    @get:JsonView(BoclipsInternalProjection::class)
    val currency: String? = null,
    @get:JsonView(BoclipsInternalProjection::class)
    val official: Boolean? = null,
    @get:JsonView(BoclipsInternalProjection::class)
    val distributionMethods: Set<DistributionMethodResource>? = null,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    var _links: Map<String, HateoasLink>? = null
)
