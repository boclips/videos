package com.boclips.contentpartner.service.infrastructure

import com.boclips.contentpartner.service.domain.model.AgeRange
import com.boclips.contentpartner.service.domain.model.ContentPartner
import com.boclips.contentpartner.service.domain.model.ContentPartnerId
import com.boclips.contentpartner.service.domain.model.Credit
import com.boclips.contentpartner.service.domain.model.DistributionMethod
import com.boclips.contentpartner.service.domain.model.Remittance
import com.boclips.videos.service.infrastructure.video.DistributionMethodDocument
import org.bson.types.ObjectId
import java.util.Currency
import java.util.Locale

object ContentPartnerDocumentConverter {
    fun toContentPartnerDocument(contentPartner: ContentPartner): ContentPartnerDocument {
        return ContentPartnerDocument(
            id = ObjectId(contentPartner.contentPartnerId.value),
            youtubeChannelId = when (contentPartner.credit) {
                is Credit.YoutubeCredit -> contentPartner.credit.channelId
                else -> null
            },
            name = contentPartner.name,
            ageRangeMax = contentPartner.ageRange.max(),
            ageRangeMin = contentPartner.ageRange.min(),
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
            language = contentPartner.language?.toLanguageTag()
        )
    }

    fun toContentPartner(document: ContentPartnerDocument): ContentPartner {
        return ContentPartner(
            contentPartnerId = ContentPartnerId(value = document.id.toString()),
            name = document.name,
            ageRange = if (document.ageRangeMin !== null) AgeRange.bounded(
                document.ageRangeMin,
                document.ageRangeMax
            ) else AgeRange.unbounded(),
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
            language = document.language?.let { Locale.forLanguageTag(it) }
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
}
