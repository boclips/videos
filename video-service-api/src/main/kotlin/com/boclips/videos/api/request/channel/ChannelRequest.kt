package com.boclips.videos.api.request.channel

import com.boclips.videos.api.request.validators.Language
import com.boclips.videos.api.response.channel.DistributionMethodResource
import com.boclips.videos.api.response.channel.IngestDetailsResource
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import java.time.Period
import javax.validation.constraints.NotBlank

data class ChannelRequest(
    @field:NotBlank
    val name: String? = null,
    var legalRestrictions: LegalRestrictionsRequest? = null,
    @JsonSetter(contentNulls = Nulls.FAIL)
    var ageRanges: List<String>? = null,
    val distributionMethods: Set<DistributionMethodResource>? = null,
    @field:CurrencyCode
    val currency: String? = null,
    val description: String? = null,
    @JsonSetter(contentNulls = Nulls.FAIL)
    val contentCategories: List<ContentCategoryRequest>? = null,
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
    val marketingInformation: MarketingInformationRequest? = null,
    val isTranscriptProvided: Boolean? = null,
    val educationalResources: String? = null,
    val curriculumAligned: String? = null,
    @JsonSetter(contentNulls = Nulls.FAIL)
    val bestForTags: List<String>? = null,
    @JsonSetter(contentNulls = Nulls.FAIL)
    val subjects: List<String>? = null,
    val contractId: String? = null,
    val categories: List<String>? = null,
    val videoLevelTagging: Boolean? = null,
)


