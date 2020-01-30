package com.boclips.videos.api.response.contentpartner

import com.boclips.videos.api.response.HateoasLink
import com.fasterxml.jackson.annotation.JsonInclude

data class ContentPartnerResource(
    val id: String,
    val name: String,
    val ageRange: AgeRangeResource? = null,
    val official: Boolean,
    val legalRestriction: LegalRestrictionResource? = null,
    val distributionMethods: Set<DistributionMethodResource>,
    val currency: String?,
    val description:String? = null,
    val contentCategories: List<ContentCategoryResource>? = null,
    val language: LanguageResource? = null,
    val hubspotId: String? = null,
    val awards: String? = null,
    val notes: String? = null,
    val contentTypes: List<String>? = emptyList(),


    @JsonInclude(JsonInclude.Include.NON_NULL)
    var _links: Map<String, HateoasLink>? = null
)
