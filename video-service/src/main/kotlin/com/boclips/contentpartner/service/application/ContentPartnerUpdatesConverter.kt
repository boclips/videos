package com.boclips.contentpartner.service.application

import com.boclips.contentpartner.service.domain.model.AgeRange
import com.boclips.contentpartner.service.domain.model.ContentPartnerId
import com.boclips.contentpartner.service.domain.model.ContentPartnerUpdateCommand
import com.boclips.contentpartner.service.domain.model.ContentPartnerUpdateCommand.ReplaceDistributionMethods
import com.boclips.contentpartner.service.domain.model.LegalRestrictionsId
import com.boclips.contentpartner.service.domain.model.LegalRestrictionsRepository
import com.boclips.contentpartner.service.presentation.DistributionMethodResourceConverter
import com.boclips.videos.api.request.contentpartner.CreateContentPartnerRequest
import java.util.Currency

class ContentPartnerUpdatesConverter(private val legalRestrictionsRepository: LegalRestrictionsRepository) {
    fun convert(id: ContentPartnerId, createContentPartnerRequest: CreateContentPartnerRequest): List<ContentPartnerUpdateCommand> {
        val commandCreator = ContentPartnerUpdateCommandCreator(id, createContentPartnerRequest)
        return listOfNotNull(
            commandCreator.updateNameOrNot(),
            commandCreator.updateAgeRangeOrNot(),
            commandCreator.updateLegalRestrictionsOrNot(legalRestrictionsRepository),
            commandCreator.updateHiddenDeliveryMethodsOrNot(),
            commandCreator.updateCurrencyOrNot()
        )
    }
}

class ContentPartnerUpdateCommandCreator(
    val id: ContentPartnerId,
    private val createContentPartnerRequest: CreateContentPartnerRequest
) {

    fun updateHiddenDeliveryMethodsOrNot(): ContentPartnerUpdateCommand? {
        return createContentPartnerRequest.distributionMethods
            ?.let {
                DistributionMethodResourceConverter.toDistributionMethods(it)
            }
            ?.let { deliveryMethods ->
                ReplaceDistributionMethods(contentPartnerId = id, distributionMethods = deliveryMethods)
            }
    }

    fun updateNameOrNot(): ContentPartnerUpdateCommand.ReplaceName? =
        createContentPartnerRequest.name?.let {
            ContentPartnerUpdateCommand.ReplaceName(contentPartnerId = id, name = it)
        }

    fun updateAgeRangeOrNot(): ContentPartnerUpdateCommand.ReplaceAgeRange? =
        createContentPartnerRequest.ageRange?.let {
            ContentPartnerUpdateCommand.ReplaceAgeRange(id, AgeRange.bounded(min = it.min, max = it.max))
        }

    fun updateLegalRestrictionsOrNot(legalRestrictionsRepository: LegalRestrictionsRepository): ContentPartnerUpdateCommand.ReplaceLegalRestrictions? =
        createContentPartnerRequest.legalRestrictions
            ?.let { restrictionsRequest -> legalRestrictionsRepository.findById(LegalRestrictionsId(restrictionsRequest.id!!)) }
            ?.let { restrictions -> ContentPartnerUpdateCommand.ReplaceLegalRestrictions(id, restrictions) }

    fun updateCurrencyOrNot(): ContentPartnerUpdateCommand.ReplaceCurrency? =
        createContentPartnerRequest.currency?.let {
            ContentPartnerUpdateCommand.ReplaceCurrency(id, Currency.getInstance(it))
        }
}
