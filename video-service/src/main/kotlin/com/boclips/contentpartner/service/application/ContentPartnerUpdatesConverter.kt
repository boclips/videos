package com.boclips.contentpartner.service.application

import com.boclips.videos.service.domain.model.common.AgeRange
import com.boclips.contentpartner.service.domain.model.ContentPartnerId
import com.boclips.contentpartner.service.domain.model.ContentPartnerUpdateCommand
import com.boclips.contentpartner.service.domain.model.ContentPartnerUpdateCommand.ReplaceDistributionMethods
import com.boclips.contentpartner.service.presentation.ContentPartnerRequest
import com.boclips.videos.service.presentation.deliveryMethod.DistributionMethodResourceConverter
import java.util.*

class ContentPartnerUpdatesConverter {
    fun convert(id: ContentPartnerId, contentPartnerRequest: ContentPartnerRequest): List<ContentPartnerUpdateCommand> {
        return listOfNotNull(
            updateNameOrNot(id = id, contentPartnerRequest = contentPartnerRequest),
            updateAgeRangeOrNot(id = id, contentPartnerRequest = contentPartnerRequest),
            updateHiddenDeliveryMethodsOrNot(id = id, contentPartnerRequest = contentPartnerRequest),
            updateCurrencyOrNot(id = id, contentPartnerRequest = contentPartnerRequest)
        )
    }

    private fun updateHiddenDeliveryMethodsOrNot(
        id: ContentPartnerId,
        contentPartnerRequest: ContentPartnerRequest
    ): ContentPartnerUpdateCommand? {
        return contentPartnerRequest.distributionMethods
            ?.let {
                DistributionMethodResourceConverter.toDistributionMethods(it)
            }
            ?.let { deliveryMethods ->
                ReplaceDistributionMethods(contentPartnerId = id, distributionMethods = deliveryMethods)
            }
    }

    private fun updateNameOrNot(
        id: ContentPartnerId,
        contentPartnerRequest: ContentPartnerRequest
    ): ContentPartnerUpdateCommand.ReplaceName? =
        contentPartnerRequest.name?.let {
            ContentPartnerUpdateCommand.ReplaceName(contentPartnerId = id, name = it)
        }

    private fun updateAgeRangeOrNot(
        id: ContentPartnerId, contentPartnerRequest: ContentPartnerRequest
    ): ContentPartnerUpdateCommand.ReplaceAgeRange? =
        contentPartnerRequest.ageRange?.let {
            ContentPartnerUpdateCommand.ReplaceAgeRange(id, AgeRange.bounded(min = it.min, max = it.max))
        }

    private fun updateCurrencyOrNot(
        id: ContentPartnerId, contentPartnerRequest: ContentPartnerRequest
    ): ContentPartnerUpdateCommand.ReplaceCurrency? =
        contentPartnerRequest.currency?.let {
            ContentPartnerUpdateCommand.ReplaceCurrency(id, Currency.getInstance(it))
        }
}
