package com.boclips.contentpartner.service.presentation

import com.boclips.contentpartner.service.application.CurrencyCode
import com.boclips.contentpartner.service.presentation.ageRange.AgeRangeRequest
import javax.validation.Valid
import javax.validation.constraints.NotBlank

data class ContentPartnerRequest(
    @field:NotBlank
    val name: String? = null,
    val accreditedToYtChannelId: String? = null,
    var legalRestrictions: LegalRestrictionsRequest? = null,
    @field:Valid var ageRange: AgeRangeRequest? = null,
    val distributionMethods: Set<DistributionMethodResource>? = null,
    @field:CurrencyCode
    val currency: String? = null
)
