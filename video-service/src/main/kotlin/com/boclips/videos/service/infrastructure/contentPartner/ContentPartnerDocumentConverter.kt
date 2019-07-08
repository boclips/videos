package com.boclips.videos.service.infrastructure.contentPartner

import com.boclips.videos.service.domain.model.common.AgeRange
import com.boclips.videos.service.domain.model.contentPartner.ContentPartner
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerId
import com.boclips.videos.service.domain.model.contentPartner.Credit
import com.boclips.videos.service.domain.model.video.DeliveryMethod
import com.boclips.videos.service.infrastructure.video.mongo.DeliveryMethodDocument
import com.boclips.videos.service.infrastructure.video.mongo.converters.DeliveryMethodDocumentConverter
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
            searchable = contentPartner.searchable,
            hiddenFromSearchForDeliveryMethods = contentPartner.hiddenFromSearchForDeliveryMethods.map(
                DeliveryMethodDocumentConverter::toDocument
            ).toSet()
        )
    }

    fun toContentPartner(document: ContentPartnerDocument): ContentPartner {
        val searchable = document.searchable ?: true

        return ContentPartner(
            contentPartnerId = ContentPartnerId(value = document.id.toString()),
            name = document.name,
            ageRange = if (document.ageRangeMin !== null) AgeRange.bounded(
                document.ageRangeMin,
                document.ageRangeMax
            ) else AgeRange.unbounded(),
            credit = document.youtubeChannelId?.let { Credit.YoutubeCredit(channelId = it) } ?: Credit.PartnerCredit,
            searchable = searchable,
            hiddenFromSearchForDeliveryMethods = document.hiddenFromSearchForDeliveryMethods?.let {
                convertDeliveryMethodsFromDocument(it)
            } ?: searchableToDeliveryMethods(searchable)
        )
    }

    private fun convertDeliveryMethodsFromDocument(hiddenFromSearchForDeliveryMethods: Set<DeliveryMethodDocument>): Set<DeliveryMethod> =
        hiddenFromSearchForDeliveryMethods.map(
            DeliveryMethodDocumentConverter::fromDocument
        ).toSet()

    private fun searchableToDeliveryMethods(searchable: Boolean): Set<DeliveryMethod> {
        return if (searchable) {
            emptySet()
        } else {
            DeliveryMethod.ALL
        }
    }
}