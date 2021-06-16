package com.boclips.contentpartner.service.domain.model.channel

import com.boclips.contentpartner.service.domain.model.contract.Contract
import com.boclips.contentpartner.service.domain.model.legalrestriction.LegalRestriction
import com.boclips.videos.service.domain.model.video.channel.ContentPartnerAvailability
import java.util.Locale

data class Channel(
    val id: ChannelId,
    val name: String,
    val legalRestriction: LegalRestriction?,
    val distributionMethods: Set<DistributionMethod>,
    val remittance: Remittance?,
    val description: String?,
    val contentCategories: List<ContentCategory>? = emptyList(),
    val language: Locale?,
    val notes: String?,
    val contentTypes: List<ContentType>? = emptyList(),
    val ingest: IngestDetails,
    val pedagogyInformation: PedagogyInformation?,
    val marketingInformation: MarketingInformation?,
    val contract: Contract?,
    val taxonomy: Taxonomy?,
    val visibility: ChannelVisibility?
) {
    val currency = contract?.remittanceCurrency ?: remittance?.currency

    fun isStreamable(): Boolean {
        return distributionMethods.contains(DistributionMethod.STREAM)
    }

    fun availability() = when {
        isDownloadable() && isStreamable() -> ContentPartnerAvailability.ALL
        isDownloadable() -> ContentPartnerAvailability.DOWNLOAD
        isStreamable() -> ContentPartnerAvailability.STREAMING
        else -> ContentPartnerAvailability.NONE
    }

    private fun isDownloadable(): Boolean {
        return distributionMethods.contains(DistributionMethod.DOWNLOAD)
    }

    override fun toString(): String {
        return "Channel(id = ${this.id.value}, name = ${this.name})"
    }
}
