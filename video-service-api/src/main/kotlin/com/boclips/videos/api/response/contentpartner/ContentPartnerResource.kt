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

    @JsonInclude(JsonInclude.Include.NON_NULL)
    var _links: Map<String, HateoasLink>? = null
)
