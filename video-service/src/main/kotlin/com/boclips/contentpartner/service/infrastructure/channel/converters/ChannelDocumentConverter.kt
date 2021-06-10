package com.boclips.contentpartner.service.infrastructure.channel.converters

import com.boclips.contentpartner.service.domain.model.agerange.AgeRangeBuckets
import com.boclips.contentpartner.service.domain.model.channel.*
import com.boclips.contentpartner.service.infrastructure.agerange.AgeRangeDocument
import com.boclips.contentpartner.service.infrastructure.agerange.AgeRangeDocumentConverter
import com.boclips.contentpartner.service.infrastructure.channel.*
import com.boclips.contentpartner.service.infrastructure.contract.ContractDocumentConverter
import com.boclips.contentpartner.service.infrastructure.legalrestriction.LegalRestrictionsDocument
import com.boclips.videos.service.infrastructure.video.DistributionMethodDocument
import mu.KLogging
import org.bson.types.ObjectId
import java.net.MalformedURLException
import java.net.URL
import java.util.*

object ChannelDocumentConverter : KLogging() {
    private fun getAgeRangeBuckets(ageRangeBuckets: List<AgeRangeDocument>?) =
        ageRangeBuckets?.map { AgeRangeDocumentConverter.toAgeRange(it) }
            ?: emptyList()

    fun toChannelDocument(channel: Channel): ChannelDocument {
        return ChannelDocument(
            id = ObjectId(channel.id.value),
            name = channel.name,
            ageRanges = channel.pedagogyInformation?.ageRangeBuckets?.ageRanges?.map {
                AgeRangeDocumentConverter.toAgeRangeDocument(
                    it
                )
            },
            legalRestrictions = channel.legalRestriction?.let {
                LegalRestrictionsDocument.from(
                    it
                )
            },
            distributionMethods = channel.distributionMethods
                .map(DistributionMethodDocumentConverter::toDocument)
                .toSet(),
            remittanceCurrency = channel.remittance?.currency?.currencyCode,
            description = channel.description,
            contentCategories = channel.contentCategories?.map { it.name },
            notes = channel.notes,
            language = channel.language?.toLanguageTag(),
            contentTypes = channel.contentTypes?.map { it.name },
            ingest = IngestDetailsDocumentConverter.toIngestDetailsDocument(
                channel.ingest
            ),
            marketingInformation = channel.marketingInformation?.let {
                MarketingInformationDocument(
                    oneLineDescription = it.oneLineDescription,
                    status = when (it.status) {
                        ChannelStatus.NEEDS_INTRODUCTION -> ContentPartnerStatusDocument.NEEDS_INTRODUCTION
                        ChannelStatus.HAVE_REACHED_OUT -> ContentPartnerStatusDocument.HAVE_REACHED_OUT
                        ChannelStatus.NEEDS_CONTENT -> ContentPartnerStatusDocument.NEEDS_CONTENT
                        ChannelStatus.WAITING_FOR_INGEST -> ContentPartnerStatusDocument.WAITING_FOR_INGEST
                        ChannelStatus.SHOULD_ADD_TO_SITE -> ContentPartnerStatusDocument.SHOULD_ADD_TO_SITE
                        ChannelStatus.SHOULD_PROMOTE -> ContentPartnerStatusDocument.SHOULD_PROMOTE
                        ChannelStatus.PROMOTED -> ContentPartnerStatusDocument.PROMOTED
                        null -> null
                    },
                    logos = it.logos?.map { logoUrl -> logoUrl.toString() },
                    showreel = it.showreel?.toString(),
                    sampleVideos = it.sampleVideos?.map { sampleVideoUrl -> sampleVideoUrl.toString() }
                )
            },
            bestForTags = channel.pedagogyInformation?.bestForTags,
            subjects = channel.pedagogyInformation?.subjects,
            contract = channel.contract?.let { contract ->
                ContractDocumentConverter().toDocument(contract)
            },
            taxonomy = channel.taxonomy?.let { toCategoriesDocument(it) },
            hidden = channel.visibility == ChannelVisibility.HIDDEN
        )
    }

    private fun toCategoriesDocument(taxonomy: Taxonomy) = when (taxonomy) {
        is Taxonomy.ChannelLevelTagging -> TaxonomyDocument(
            categories = taxonomy.categories.map {
                CategoriesDocumentConverter.toDocument(it)
            }.toSet()
        )
        is Taxonomy.VideoLevelTagging -> TaxonomyDocument(
            requiresVideoLevelTagging = true
        )
    }

    fun toChannel(document: ChannelDocument): Channel {
        return Channel(
            id = ChannelId(
                value = document.id.toString()
            ),
            name = document.name,
            legalRestriction = document.legalRestrictions?.toRestrictions(),
            distributionMethods = reconstructDistributionMethods(
                document
            ),
            remittance = document.remittanceCurrency?.let {
                Remittance(
                    Currency.getInstance(it)
                )
            },
            description = document.description,
            contentCategories = document.contentCategories?.map { ContentCategory.valueOf(it) },
            notes = document.notes,
            language = document.language?.let { Locale.forLanguageTag(it) },
            contentTypes = document.contentTypes?.mapNotNull {
                when (it) {
                    "NEWS" -> ContentType.NEWS
                    "INSTRUCTIONAL" -> ContentType.INSTRUCTIONAL
                    "STOCK" -> ContentType.STOCK
                    else -> {
                        logger.warn {
                            "$it is not a valid type. Valid types are ${
                                ContentType.values()
                                    .joinToString(prefix = "[", postfix = "]") { value -> value.name }
                            }"
                        }
                        null
                    }
                }
            },
            ingest = document.ingest?.let(IngestDetailsDocumentConverter::toIngestDetails) ?: ManualIngest,
            marketingInformation = document.marketingInformation?.let {
                MarketingInformation(
                    oneLineDescription = it.oneLineDescription,
                    status = when (it.status) {
                        ContentPartnerStatusDocument.NEEDS_INTRODUCTION -> ChannelStatus.NEEDS_INTRODUCTION
                        ContentPartnerStatusDocument.HAVE_REACHED_OUT -> ChannelStatus.HAVE_REACHED_OUT
                        ContentPartnerStatusDocument.NEEDS_CONTENT -> ChannelStatus.NEEDS_CONTENT
                        ContentPartnerStatusDocument.WAITING_FOR_INGEST -> ChannelStatus.WAITING_FOR_INGEST
                        ContentPartnerStatusDocument.SHOULD_ADD_TO_SITE -> ChannelStatus.SHOULD_ADD_TO_SITE
                        ContentPartnerStatusDocument.SHOULD_PROMOTE -> ChannelStatus.SHOULD_PROMOTE
                        ContentPartnerStatusDocument.PROMOTED -> ChannelStatus.PROMOTED
                        null -> null
                    },
                    logos = it.logos?.mapNotNull(ChannelDocumentConverter::safeToUrl),
                    showreel = it.showreel?.let(ChannelDocumentConverter::safeToUrl),
                    sampleVideos = it.sampleVideos?.mapNotNull(ChannelDocumentConverter::safeToUrl)
                )
            },
            pedagogyInformation = PedagogyInformation(
                bestForTags = document.bestForTags,
                subjects = document.subjects,
                ageRangeBuckets = AgeRangeBuckets(
                    ageRanges = getAgeRangeBuckets(
                        document.ageRanges
                    )
                )
            ),
            contract = document.contract?.let {
                ContractDocumentConverter().toContract(it)
            },
            taxonomy = document.taxonomy?.let {
                convertTaxonomyDocument(it)
            },
            visibility = document.hidden?.let {
                if (it) {
                    ChannelVisibility.HIDDEN
                } else {
                    ChannelVisibility.VISIBLE
                }
            } ?: ChannelVisibility.VISIBLE
        )
    }

    private fun convertTaxonomyDocument(taxonomy: TaxonomyDocument) =
        if (taxonomy.requiresVideoLevelTagging == true) Taxonomy.VideoLevelTagging
        else Taxonomy.ChannelLevelTagging(
            categories = CategoriesDocumentConverter.fromDocument(taxonomy.categories)
        )

    private fun reconstructDistributionMethods(document: ChannelDocument): Set<DistributionMethod> {
        return document.distributionMethods?.let {
            convertDistributionMethodsFromDocument(
                it
            )
        } ?: DistributionMethod.ALL
    }

    private fun convertDistributionMethodsFromDocument(distributionMethodsDocument: Set<DistributionMethodDocument>): Set<DistributionMethod> {
        return distributionMethodsDocument
            .map(DistributionMethodDocumentConverter::fromDocument)
            .toSet()
    }

    private fun safeToUrl(s: String): URL? =
        try {
            URL(s)
        } catch (e: MalformedURLException) {
            null
        }
}
