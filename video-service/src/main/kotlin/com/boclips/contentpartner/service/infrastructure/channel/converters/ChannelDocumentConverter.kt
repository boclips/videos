package com.boclips.contentpartner.service.infrastructure.channel.converters

import com.boclips.contentpartner.service.domain.model.agerange.AgeRangeBuckets
import com.boclips.contentpartner.service.domain.model.channel.Channel
import com.boclips.contentpartner.service.domain.model.channel.ChannelId
import com.boclips.contentpartner.service.domain.model.channel.MarketingInformation
import com.boclips.contentpartner.service.domain.model.channel.ChannelStatus
import com.boclips.contentpartner.service.domain.model.channel.ContentType
import com.boclips.contentpartner.service.domain.model.channel.DistributionMethod
import com.boclips.contentpartner.service.domain.model.channel.ManualIngest
import com.boclips.contentpartner.service.domain.model.channel.PedagogyInformation
import com.boclips.contentpartner.service.domain.model.channel.Remittance
import com.boclips.contentpartner.service.infrastructure.agerange.AgeRangeDocument
import com.boclips.contentpartner.service.infrastructure.agerange.AgeRangeDocumentConverter
import com.boclips.contentpartner.service.infrastructure.channel.ChannelDocument
import com.boclips.contentpartner.service.infrastructure.channel.ContentPartnerStatusDocument
import com.boclips.contentpartner.service.infrastructure.channel.MarketingInformationDocument
import com.boclips.contentpartner.service.infrastructure.contract.ContractDocumentConverter
import com.boclips.contentpartner.service.infrastructure.legalrestriction.LegalRestrictionsDocument
import com.boclips.videos.service.infrastructure.video.DistributionMethodDocument
import mu.KLogging
import org.bson.types.ObjectId
import java.net.MalformedURLException
import java.net.URL
import java.time.Period
import java.util.Currency
import java.util.Locale

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
            contentCategories = channel.contentCategories,
            hubspotId = channel.hubspotId,
            awards = channel.awards,
            notes = channel.notes,
            language = channel.language?.toLanguageTag(),
            contentTypes = channel.contentTypes?.map { it.name },
            ingest = IngestDetailsDocumentConverter.toIngestDetailsDocument(
                channel.ingest
            ),
            deliveryFrequency = channel.deliveryFrequency?.toString(),
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
            isTranscriptProvided = channel.pedagogyInformation?.isTranscriptProvided,
            educationalResources = channel.pedagogyInformation?.educationalResources,
            curriculumAligned = channel.pedagogyInformation?.curriculumAligned,
            bestForTags = channel.pedagogyInformation?.bestForTags,
            subjects = channel.pedagogyInformation?.subjects,
            contract = channel.contract?.let { contract ->
                ContractDocumentConverter().toDocument(contract)
            }
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
            contentCategories = document.contentCategories,
            hubspotId = document.hubspotId,
            awards = document.awards,
            notes = document.notes,
            language = document.language?.let { Locale.forLanguageTag(it) },
            contentTypes = document.contentTypes?.mapNotNull {
                when (it) {
                    "NEWS" -> ContentType.NEWS
                    "INSTRUCTIONAL" -> ContentType.INSTRUCTIONAL
                    "STOCK" -> ContentType.STOCK
                    else -> {
                        logger.warn {
                            "$it is not a valid type. Valid types are ${ContentType.values()
                                .joinToString(prefix = "[", postfix = "]") { value -> value.name }}"
                        }
                        null
                    }
                }
            },
            ingest = document.ingest?.let(IngestDetailsDocumentConverter::toIngestDetails) ?: ManualIngest,
            deliveryFrequency = document.deliveryFrequency?.let { Period.parse(it) },
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
                isTranscriptProvided = document.isTranscriptProvided,
                educationalResources = document.educationalResources,
                curriculumAligned = document.curriculumAligned,
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
            }
        )
    }

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
