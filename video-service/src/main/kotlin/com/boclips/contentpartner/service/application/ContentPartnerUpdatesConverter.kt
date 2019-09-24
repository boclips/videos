package com.boclips.contentpartner.service.application

import com.boclips.contentpartner.service.domain.model.ContentPartnerId
import com.boclips.contentpartner.service.domain.model.ContentPartnerUpdateCommand
import com.boclips.contentpartner.service.domain.model.ContentPartnerUpdateCommand.ReplaceDistributionMethods
import com.boclips.contentpartner.service.domain.model.LegalRestrictionsId
import com.boclips.contentpartner.service.domain.model.LegalRestrictionsRepository
import com.boclips.contentpartner.service.presentation.ContentPartnerRequest
import com.boclips.videos.service.domain.model.common.AgeRange
import com.boclips.videos.service.presentation.deliveryMethod.DistributionMethodResourceConverter
import java.util.*

class ContentPartnerUpdatesConverter(private val legalRestrictionsRepository: LegalRestrictionsRepository) {
    fun convert(id: ContentPartnerId, contentPartnerRequest: ContentPartnerRequest): List<ContentPartnerUpdateCommand> {
        val commandCreator = ContentPartnerUpdateCommandCreator(id, contentPartnerRequest)
        return listOfNotNull(
            commandCreator.updateNameOrNot(),
            commandCreator.updateAgeRangeOrNot(),
            commandCreator.updateLegalRestrictionsOrNot(legalRestrictionsRepository),
            commandCreator.updateHiddenDeliveryMethodsOrNot(),
            commandCreator.updateCurrencyOrNot()
        )
    }
}

class ContentPartnerUpdateCommandCreator(val id: ContentPartnerId, val contentPartnerRequest: ContentPartnerRequest) {

    fun updateHiddenDeliveryMethodsOrNot(): ContentPartnerUpdateCommand? {
        return contentPartnerRequest.distributionMethods
            ?.let {
                DistributionMethodResourceConverter.toDistributionMethods(it)
            }
            ?.let { deliveryMethods ->
                ReplaceDistributionMethods(contentPartnerId = id, distributionMethods = deliveryMethods)
            }
    }

    fun updateNameOrNot(): ContentPartnerUpdateCommand.ReplaceName? =
        contentPartnerRequest.name?.let {
            ContentPartnerUpdateCommand.ReplaceName(contentPartnerId = id, name = it)
        }

    fun updateAgeRangeOrNot(): ContentPartnerUpdateCommand.ReplaceAgeRange? =
        contentPartnerRequest.ageRange?.let {
            ContentPartnerUpdateCommand.ReplaceAgeRange(id, AgeRange.bounded(min = it.min, max = it.max))
        }

    fun updateLegalRestrictionsOrNot(legalRestrictionsRepository: LegalRestrictionsRepository): ContentPartnerUpdateCommand.ReplaceLegalRestrictions? =
        contentPartnerRequest.legalRestrictions
            ?.let { restrictionsRequest -> legalRestrictionsRepository.findById(LegalRestrictionsId(restrictionsRequest.id!!)) }
            ?.let { restrictions -> ContentPartnerUpdateCommand.ReplaceLegalRestrictions(id, restrictions) }

    fun updateCurrencyOrNot(): ContentPartnerUpdateCommand.ReplaceCurrency? =
        contentPartnerRequest.currency?.let {
            ContentPartnerUpdateCommand.ReplaceCurrency(id, Currency.getInstance(it))
        }
}
