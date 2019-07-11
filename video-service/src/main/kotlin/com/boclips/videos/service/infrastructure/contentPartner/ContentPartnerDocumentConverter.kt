package com.boclips.videos.service.infrastructure.contentPartner

import com.boclips.videos.service.domain.model.common.AgeRange
import com.boclips.videos.service.domain.model.contentPartner.ContentPartner
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerId
import com.boclips.videos.service.domain.model.contentPartner.Credit
import com.boclips.videos.service.domain.model.video.DistributionMethod
import com.boclips.videos.service.infrastructure.video.mongo.DistributionMethodDocument
import com.boclips.videos.service.infrastructure.video.mongo.converters.DistributionMethodDocumentConverter
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
            disabledDistributionMethods = convertToDisabledDistributionMethods(contentPartner)
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
            distributionMethods = document.disabledDistributionMethods?.let {
                convertDistributionMethodsFromDocument(it)
            } ?: DistributionMethod.ALL
        )
    }

    private fun convertToDisabledDistributionMethods(contentPartner: ContentPartner): Set<DistributionMethodDocument> {
        val disabledDistributionMethods = DistributionMethod.ALL - contentPartner.distributionMethods

        return disabledDistributionMethods.map(DistributionMethodDocumentConverter::toDocument).toSet()
    }

    private fun convertDistributionMethodsFromDocument(distributionMethodsDocument: Set<DistributionMethodDocument>): Set<DistributionMethod> {
        val disabledDistributionMethods = distributionMethodsDocument
            .map(DistributionMethodDocumentConverter::fromDocument)
            .toSet()

        return DistributionMethod.ALL - disabledDistributionMethods
    }
}
