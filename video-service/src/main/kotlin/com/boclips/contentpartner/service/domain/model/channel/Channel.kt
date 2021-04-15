package com.boclips.contentpartner.service.domain.model.channel

import com.boclips.contentpartner.service.domain.model.contract.Contract
import com.boclips.contentpartner.service.domain.model.legalrestriction.LegalRestriction
import com.boclips.videos.service.domain.model.taxonomy.TaxonomyCategory
import com.boclips.videos.service.domain.model.video.channel.Availability
import java.time.Period
import java.util.Locale

data class Channel(
    val id: ChannelId,
    val name: String,
    val legalRestriction: LegalRestriction?,
    val distributionMethods: Set<DistributionMethod>,
    val remittance: Remittance?,
    val description: String?,
    val contentCategories: List<ContentCategory>? = emptyList(),
    val hubspotId: String?,
    val language: Locale?,
    val awards: String?,
    val notes: String?,
    val contentTypes: List<ContentType>? = emptyList(),
    val ingest: IngestDetails,
    val deliveryFrequency: Period?,
    val pedagogyInformation: PedagogyInformation?,
    val marketingInformation: MarketingInformation?,
    val contract: Contract?,
    val categories:List<TaxonomyCategory>? = emptyList()
) {
    val currency = contract?.remittanceCurrency ?: remittance?.currency

    fun isStreamable(): Boolean {
        return distributionMethods.contains(DistributionMethod.STREAM)
    }

    fun availability() = when {
        isDownloadable() && isStreamable() -> Availability.ALL
        isDownloadable() -> Availability.DOWNLOAD
        isStreamable() -> Availability.STREAMING
        else -> Availability.NONE
    }

    private fun isDownloadable(): Boolean {
        return distributionMethods.contains(DistributionMethod.DOWNLOAD)
    }

    override fun toString(): String {
        return "Channel(id = ${this.id.value}, name = ${this.name})"
    }
}
