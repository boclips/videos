package com.boclips.contentpartner.service.domain.model.channel

import com.boclips.contentpartner.service.domain.model.contract.Contract
import com.boclips.contentpartner.service.domain.model.legalrestriction.LegalRestriction
import java.time.Period
import java.util.Locale

data class Channel(
    val id: ChannelId,
    val name: String,
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
    val contentTypes: List<ContentType>? = emptyList(),
    val ingest: IngestDetails,
    val deliveryFrequency: Period?,
    val pedagogyInformation: PedagogyInformation?,
    val marketingInformation: MarketingInformation?,
    val contract: Contract?
) {
    val currency = contract?.remittanceCurrency ?: remittance?.currency

    fun isStreamable(): Boolean {
        return distributionMethods.contains(DistributionMethod.STREAM)
    }

    fun isDownloadable(): Boolean {
        return distributionMethods.contains(DistributionMethod.DOWNLOAD)
    }

    override fun toString(): String {
        return "Channel(id = ${this.id.value}, name = ${this.name})"
    }
}
