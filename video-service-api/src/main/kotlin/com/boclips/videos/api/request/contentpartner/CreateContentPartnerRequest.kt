package com.boclips.videos.api.request.contentpartner

import com.boclips.videos.api.request.validators.Language
import com.boclips.videos.api.response.contentpartner.DistributionMethodResource
import javax.validation.Valid
import javax.validation.constraints.NotBlank

data class CreateContentPartnerRequest(
    @field:NotBlank
    val name: String? = null,
    val accreditedToYtChannelId: String? = null,
    var legalRestrictions: LegalRestrictionsRequest? = null,
    @field:Valid var ageRange: AgeRangeRequest? = null,
    val distributionMethods: Set<DistributionMethodResource>? = null,
    @field:CurrencyCode
    val currency: String? = null,
    val description: String? = null,
    val contentCategories: List<String>? = emptyList(),
    val hubspotId: String? = null,
    val awards: String? = null,
    val notes: String? = null,
    @field:Language
    val language: String? = null
)
