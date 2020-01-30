package com.boclips.contentpartner.service.domain.model

import java.util.Locale

data class ContentPartner(
    val contentPartnerId: ContentPartnerId,
    val name: String,
    val ageRange: AgeRange,
    val credit: Credit,
    val legalRestriction: LegalRestriction?,
    val distributionMethods: Set<DistributionMethod>,
    val remittance: Remittance?,
    val description: String?,
    val contentCategories: List<String>? = emptyList(),
    val hubspotId: String? = null,
    val language: Locale? = null,
    val awards: String? = null,
    val notes: String? = null
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
