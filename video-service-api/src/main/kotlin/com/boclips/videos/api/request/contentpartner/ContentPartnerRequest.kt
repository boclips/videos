package com.boclips.videos.api.request.contentpartner

import com.boclips.videos.api.request.validators.Language
import com.boclips.videos.api.response.contentpartner.DistributionMethodResource
import com.boclips.videos.api.response.contentpartner.IngestDetailsResource
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import java.time.Period
import javax.validation.constraints.NotBlank

data class ContentPartnerRequest(
    @field:NotBlank
    val name: String? = null,
    val accreditedToYtChannelId: String? = null,
    var legalRestrictions: LegalRestrictionsRequest? = null,
    @JsonSetter(contentNulls = Nulls.FAIL)
    var ageRanges: List<String>? = null,
    val distributionMethods: Set<DistributionMethodResource>? = null,
    @field:CurrencyCode
    val currency: String? = null,
    val description: String? = null,
    @JsonSetter(contentNulls = Nulls.FAIL)
    val contentCategories: List<String>? = null,
    val hubspotId: String? = null,
    val awards: String? = null,
    val notes: String? = null,
    @field:Language
    val language: String? = null,
    @JsonSetter(contentNulls = Nulls.FAIL)
    val contentTypes: List<String>? = null,
    val ingest: IngestDetailsResource? = null,
    val deliveryFrequency: Period? = null,
    val oneLineDescription: String? = null,
    val marketingInformation: ContentPartnerMarketingInformationRequest? = null,
    val isTranscriptProvided: Boolean? = null,
    val educationalResources: String? = null,
    val curriculumAligned: String? = null,
    @JsonSetter(contentNulls = Nulls.FAIL)
    val bestForTags: List<String>? = null,
    @JsonSetter(contentNulls = Nulls.FAIL)
    val subjects: List<String>? = null
)


