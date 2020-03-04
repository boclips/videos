package com.boclips.contentpartner.service.infrastructure

import com.boclips.contentpartner.service.domain.model.AgeRangeBuckets
import com.boclips.contentpartner.service.domain.model.ContentPartner
import com.boclips.contentpartner.service.domain.model.ContentPartnerId
import com.boclips.contentpartner.service.domain.model.ContentPartnerMarketingInformation
import com.boclips.contentpartner.service.domain.model.ContentPartnerStatus
import com.boclips.contentpartner.service.domain.model.ContentPartnerType
import com.boclips.contentpartner.service.domain.model.Credit
import com.boclips.contentpartner.service.domain.model.DistributionMethod
import com.boclips.contentpartner.service.domain.model.ManualIngest
import com.boclips.contentpartner.service.domain.model.PedagogyInformation
import com.boclips.contentpartner.service.domain.model.Remittance
import com.boclips.videos.api.common.ExplicitlyNull
import com.boclips.videos.api.common.Specified
import com.boclips.videos.service.infrastructure.video.DistributionMethodDocument
import org.bson.types.ObjectId
import java.net.MalformedURLException
import java.net.URL
import java.util.Currency
import java.util.Locale

object ContentPartnerDocumentConverter {
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
            legalRestrictions = contentPartner.legalRestriction?.let { LegalRestrictionsDocument.from(it) },
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
            ingest = IngestDetailsDocumentConverter.toIngestDetailsDocument(contentPartner.ingest),
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
                    showreel = it.showreel?.let { showreel ->
                        when (showreel) {
                            is Specified -> showreel.value.toString()
                            is ExplicitlyNull -> null
                        }
                    },
                    sampleVideos = it.sampleVideos?.map { sampleVideoUrl -> sampleVideoUrl.toString() }
                )
            },
            isTranscriptProvided = contentPartner.pedagogyInformation?.isTranscriptProvided,
            educationalResources = contentPartner.pedagogyInformation?.educationalResources,
            curriculumAligned = contentPartner.pedagogyInformation?.curriculumAligned,
            bestForTags = contentPartner.pedagogyInformation?.bestForTags,
            subjects = contentPartner.pedagogyInformation?.subjects
        )
    }

    fun toContentPartner(document: ContentPartnerDocument): ContentPartner {
        return ContentPartner(
            contentPartnerId = ContentPartnerId(value = document.id.toString()),
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
            remittance = document.remittanceCurrency?.let { Remittance(Currency.getInstance(it)) },
            description = document.description,
            contentCategories = document.contentCategories,
            hubspotId = document.hubspotId,
            awards = document.awards,
            notes = document.notes,
            language = document.language?.let { Locale.forLanguageTag(it) },
            contentTypes = document.contentTypes?.map { ContentPartnerType.valueOf(it) },
            ingest = document.ingest?.let(IngestDetailsDocumentConverter::toIngestDetails) ?: ManualIngest,
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
                    logos = it.logos?.mapNotNull(::safeToUrl),
                    showreel = it.showreel?.let(::safeToUrl)?.let { url -> Specified(url) },
                    sampleVideos = it.sampleVideos?.mapNotNull(::safeToUrl)
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
            )

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
