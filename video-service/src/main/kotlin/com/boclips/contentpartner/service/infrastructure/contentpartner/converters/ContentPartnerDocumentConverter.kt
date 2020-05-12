package com.boclips.contentpartner.service.infrastructure.contentpartner.converters

import com.boclips.contentpartner.service.domain.model.agerange.AgeRangeBuckets
import com.boclips.contentpartner.service.domain.model.contentpartner.ContentPartner
import com.boclips.contentpartner.service.domain.model.contentpartner.ContentPartnerId
import com.boclips.contentpartner.service.domain.model.contentpartner.ContentPartnerMarketingInformation
import com.boclips.contentpartner.service.domain.model.contentpartner.ContentPartnerStatus
import com.boclips.contentpartner.service.domain.model.contentpartner.ContentPartnerType
import com.boclips.contentpartner.service.domain.model.contentpartner.Credit
import com.boclips.contentpartner.service.domain.model.contentpartner.DistributionMethod
import com.boclips.contentpartner.service.domain.model.contentpartner.ManualIngest
import com.boclips.contentpartner.service.domain.model.contentpartner.PedagogyInformation
import com.boclips.contentpartner.service.domain.model.contentpartner.Remittance
import com.boclips.contentpartner.service.infrastructure.agerange.AgeRangeDocument
import com.boclips.contentpartner.service.infrastructure.agerange.AgeRangeDocumentConverter
import com.boclips.contentpartner.service.infrastructure.contentpartner.ContentPartnerDocument
import com.boclips.contentpartner.service.infrastructure.contentpartner.ContentPartnerStatusDocument
import com.boclips.contentpartner.service.infrastructure.contentpartner.MarketingInformationDocument
import com.boclips.contentpartner.service.infrastructure.contract.ContentPartnerContractDocumentConverter
import com.boclips.contentpartner.service.infrastructure.legalrestriction.LegalRestrictionsDocument
import com.boclips.videos.api.common.ExplicitlyNull
import com.boclips.videos.api.common.Specified
import com.boclips.videos.service.infrastructure.video.DistributionMethodDocument
import mu.KLogging
import org.bson.types.ObjectId
import java.net.MalformedURLException
import java.net.URL
import java.time.Period
import java.util.Currency
import java.util.Locale

object ContentPartnerDocumentConverter : KLogging() {
    private fun getAgeRangeBuckets(ageRangeBuckets: List<AgeRangeDocument>?) =
        ageRangeBuckets?.map { AgeRangeDocumentConverter.toAgeRange(it) }
            ?: emptyList()

    fun toContentPartnerDocument(contentPartner: ContentPartner): ContentPartnerDocument {
        return ContentPartnerDocument(
            id = ObjectId(contentPartner.contentPartnerId.value),
            youtubeChannelId = when (contentPartner.credit) {
                is Credit.YoutubeCredit -> contentPartner.credit.channelId
                else -> null
            },
            name = contentPartner.name,
            ageRanges = contentPartner.ageRangeBuckets.ageRanges.map {
                AgeRangeDocumentConverter.toAgeRangeDocument(
                    it
                )
            },
            legalRestrictions = contentPartner.legalRestriction?.let {
                LegalRestrictionsDocument.from(
                    it
                )
            },
            distributionMethods = contentPartner.distributionMethods
                .map(DistributionMethodDocumentConverter::toDocument)
                .toSet(),
            remittanceCurrency = contentPartner.remittance?.currency?.currencyCode,
            description = contentPartner.description,
            contentCategories = contentPartner.contentCategories,
            hubspotId = contentPartner.hubspotId,
            awards = contentPartner.awards,
            notes = contentPartner.notes,
            language = contentPartner.language?.toLanguageTag(),
            contentTypes = contentPartner.contentTypes?.map { it.name },
            ingest = IngestDetailsDocumentConverter.toIngestDetailsDocument(
                contentPartner.ingest
            ),
            deliveryFrequency = contentPartner.deliveryFrequency?.toString(),
            marketingInformation = contentPartner.marketingInformation?.let {
                MarketingInformationDocument(
                    oneLineDescription = it.oneLineDescription,
                    status = when (it.status) {
                        ContentPartnerStatus.NEEDS_INTRODUCTION -> ContentPartnerStatusDocument.NEEDS_INTRODUCTION
                        ContentPartnerStatus.HAVE_REACHED_OUT -> ContentPartnerStatusDocument.HAVE_REACHED_OUT
                        ContentPartnerStatus.NEEDS_CONTENT -> ContentPartnerStatusDocument.NEEDS_CONTENT
                        ContentPartnerStatus.WAITING_FOR_INGEST -> ContentPartnerStatusDocument.WAITING_FOR_INGEST
                        ContentPartnerStatus.SHOULD_ADD_TO_SITE -> ContentPartnerStatusDocument.SHOULD_ADD_TO_SITE
                        ContentPartnerStatus.SHOULD_PROMOTE -> ContentPartnerStatusDocument.SHOULD_PROMOTE
                        ContentPartnerStatus.PROMOTED -> ContentPartnerStatusDocument.PROMOTED
                        null -> null
                    },
                    logos = it.logos?.map { logoUrl -> logoUrl.toString() },
                    showreel = it.showreel?.toString(),
                    sampleVideos = it.sampleVideos?.map { sampleVideoUrl -> sampleVideoUrl.toString() }
                )
            },
            isTranscriptProvided = contentPartner.pedagogyInformation?.isTranscriptProvided,
            educationalResources = contentPartner.pedagogyInformation?.educationalResources,
            curriculumAligned = contentPartner.pedagogyInformation?.curriculumAligned,
            bestForTags = contentPartner.pedagogyInformation?.bestForTags,
            subjects = contentPartner.pedagogyInformation?.subjects,
            contract = contentPartner.contract?.let { contract ->
                ContentPartnerContractDocumentConverter().toDocument(contract)
            }
        )
    }

    fun toContentPartner(document: ContentPartnerDocument): ContentPartner {
        return ContentPartner(
            contentPartnerId = ContentPartnerId(
                value = document.id.toString()
            ),
            name = document.name,
            ageRangeBuckets = AgeRangeBuckets(
                ageRanges = getAgeRangeBuckets(
                    document.ageRanges
                )
            ),
            credit = document.youtubeChannelId?.let {
                Credit.YoutubeCredit(
                    channelId = it
                )
            } ?: Credit.PartnerCredit,
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
                    "NEWS" -> ContentPartnerType.NEWS
                    "INSTRUCTIONAL" -> ContentPartnerType.INSTRUCTIONAL
                    "STOCK" -> ContentPartnerType.STOCK
                    else -> {
                        logger.warn {
                            "$it is not a valid type. Valid types are ${ContentPartnerType.values()
                                .joinToString(prefix = "[", postfix = "]") { value -> value.name }}"
                        }
                        null
                    }
                }
            },
            ingest = document.ingest?.let(IngestDetailsDocumentConverter::toIngestDetails) ?: ManualIngest,
            deliveryFrequency = document.deliveryFrequency?.let { Period.parse(it) },
            marketingInformation = document.marketingInformation?.let {
                ContentPartnerMarketingInformation(
                    oneLineDescription = it.oneLineDescription,
                    status = when (it.status) {
                        ContentPartnerStatusDocument.NEEDS_INTRODUCTION -> ContentPartnerStatus.NEEDS_INTRODUCTION
                        ContentPartnerStatusDocument.HAVE_REACHED_OUT -> ContentPartnerStatus.HAVE_REACHED_OUT
                        ContentPartnerStatusDocument.NEEDS_CONTENT -> ContentPartnerStatus.NEEDS_CONTENT
                        ContentPartnerStatusDocument.WAITING_FOR_INGEST -> ContentPartnerStatus.WAITING_FOR_INGEST
                        ContentPartnerStatusDocument.SHOULD_ADD_TO_SITE -> ContentPartnerStatus.SHOULD_ADD_TO_SITE
                        ContentPartnerStatusDocument.SHOULD_PROMOTE -> ContentPartnerStatus.SHOULD_PROMOTE
                        ContentPartnerStatusDocument.PROMOTED -> ContentPartnerStatus.PROMOTED
                        null -> null
                    },
                    logos = it.logos?.mapNotNull(ContentPartnerDocumentConverter::safeToUrl),
                    showreel = it.showreel?.let(ContentPartnerDocumentConverter::safeToUrl),
                    sampleVideos = it.sampleVideos?.mapNotNull(ContentPartnerDocumentConverter::safeToUrl)
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
                ContentPartnerContractDocumentConverter().toContract(it)
            }
        )
    }

    private fun reconstructDistributionMethods(document: ContentPartnerDocument): Set<DistributionMethod> {
        return document.distributionMethods?.let {
            convertDistributionMethodsFromDocument(
                it
            )
        } ?: convertToDefaultDistributionMethods(
            document
        )
    }

    private fun convertToDefaultDistributionMethods(contentPartnerDocument: ContentPartnerDocument): Set<DistributionMethod> {
        return if (!contentPartnerDocument.youtubeChannelId.isNullOrBlank()) {
            setOf(DistributionMethod.STREAM)
        } else {
            DistributionMethod.ALL
        }
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
