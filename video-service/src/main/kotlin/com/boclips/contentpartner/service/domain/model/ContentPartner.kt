package com.boclips.contentpartner.service.domain.model

import java.util.Locale

data class ContentPartner(
    val contentPartnerId: ContentPartnerId,
    val name: String,
    val ageRangeBuckets: AgeRangeBuckets,
    val credit: Credit,
    val legalRestriction: LegalRestriction?,
    val distributionMethods: Set<DistributionMethod>,
    val remittance: Remittance?,
    val description: String?,
    val contentCategories: List<String>? = emptyList(),
    val hubspotId: String?,
    val language: Locale?,
    val awards: String?,
    val notes: String?,
    val contentTypes: List<ContentPartnerType>? = emptyList(),
    val marketingInformation: MarketingInformation?,
    val isTranscriptProvided: Boolean?,
    val educationalResources: String?,
    val curriculumAligned: String?,
    val bestForTags: List<String>?,
    val subjects: List<String>?
) {
    fun isStreamable(): Boolean {
        return distributionMethods.contains(DistributionMethod.STREAM)
    }

    fun isDownloadable(): Boolean {
        return distributionMethods.contains(DistributionMethod.DOWNLOAD)
    }

    override fun toString(): String {
        return "ContentPartner(id = ${this.contentPartnerId.value}, name = ${this.name})"
    }
}
