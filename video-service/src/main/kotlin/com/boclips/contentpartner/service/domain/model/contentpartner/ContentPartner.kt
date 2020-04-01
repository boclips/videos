package com.boclips.contentpartner.service.domain.model.contentpartner

import com.boclips.contentpartner.service.domain.model.agerange.AgeRangeBuckets
import com.boclips.contentpartner.service.domain.model.legalrestriction.LegalRestriction
import java.time.Period
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
    val ingest: IngestDetails,
    val deliveryFrequency: Period?,
    val pedagogyInformation: PedagogyInformation?,
    val marketingInformation: ContentPartnerMarketingInformation?
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
