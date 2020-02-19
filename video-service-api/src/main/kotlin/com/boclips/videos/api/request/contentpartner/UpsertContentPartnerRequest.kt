package com.boclips.videos.api.request.contentpartner

import com.boclips.videos.api.request.validators.Language
import com.boclips.videos.api.response.contentpartner.DistributionMethodResource
import javax.validation.constraints.NotBlank

data class UpsertContentPartnerRequest(
    @field:NotBlank
    val name: String? = null,
    val accreditedToYtChannelId: String? = null,
    var legalRestrictions: LegalRestrictionsRequest? = null,
    var ageRanges: List<String>? = null,
    val distributionMethods: Set<DistributionMethodResource>? = null,
    @field:CurrencyCode
    val currency: String? = null,
    val description: String? = null,
    val contentCategories: List<String>? = null,
    val hubspotId: String? = null,
    val awards: String? = null,
    val notes: String? = null,
    @field:Language
    val language: String? = null,
    val contentTypes: List<String>? = null,
    val oneLineDescription: String? = null,
    val marketingInformation: ContentPartnerMarketingRequest? = null,
    val isTranscriptProvided: Boolean? = null,
    val educationalResources: String? = null,
    val curriculumAligned: String? = null
)
