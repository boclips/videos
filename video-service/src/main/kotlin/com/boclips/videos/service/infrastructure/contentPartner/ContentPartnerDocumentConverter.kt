package com.boclips.videos.service.infrastructure.contentPartner

import com.boclips.videos.service.domain.model.common.AgeRange
import com.boclips.videos.service.domain.model.contentPartner.ContentPartner
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerId
import com.boclips.videos.service.domain.model.contentPartner.Credit
import com.boclips.videos.service.domain.model.video.DistributionMethod
import com.boclips.videos.service.infrastructure.video.DistributionMethodDocument
import com.boclips.videos.service.infrastructure.video.converters.DistributionMethodDocumentConverter
import org.bson.types.ObjectId

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
            distributionMethods = contentPartner.distributionMethods
                .map(DistributionMethodDocumentConverter::toDocument)
                .toSet()
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
            credit = document.youtubeChannelId?.let { Credit.YoutubeCredit(channelId = it) } ?: Credit.PartnerCredit,
            distributionMethods = reconstructDistributionMethods(document)
        )
    }

    private fun reconstructDistributionMethods(document: ContentPartnerDocument): Set<DistributionMethod> {
        return document.distributionMethods?.let {
            convertDistributionMethodsFromDocument(it)
        } ?: convertToDefaultDistributionMethods(document)
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
