package com.boclips.contentpartner.service.application

import com.boclips.contentpartner.service.domain.model.AgeRange
import com.boclips.contentpartner.service.domain.model.AgeRangeComparator
import com.boclips.contentpartner.service.domain.model.ContentPartner
import com.boclips.contentpartner.service.domain.model.ContentPartnerId
import com.boclips.contentpartner.service.domain.model.ContentPartnerUpdateCommand
import com.boclips.contentpartner.service.domain.model.ContentPartnerUpdateCommand.ReplaceDistributionMethods
import com.boclips.contentpartner.service.domain.model.LegalRestrictionsId
import com.boclips.contentpartner.service.domain.model.LegalRestrictionsRepository
import com.boclips.contentpartner.service.presentation.DistributionMethodResourceConverter
import com.boclips.videos.api.request.contentpartner.UpsertContentPartnerRequest
import java.util.Currency

class ContentPartnerUpdatesConverter(private val legalRestrictionsRepository: LegalRestrictionsRepository) {
    fun convert(
        id: ContentPartnerId,
        upsertContentPartnerRequest: UpsertContentPartnerRequest,
        contentPartner: ContentPartner
    ): List<ContentPartnerUpdateCommand> {

        val commandCreator = ContentPartnerUpdateCommandCreator(id, upsertContentPartnerRequest)

        val updateFields: MutableList<ContentPartnerUpdateCommand> = arrayListOf()

        if (contentPartner.name != upsertContentPartnerRequest.name) {
            commandCreator.updateName()?.let { updateFields.add(it) }
        }

        if (AgeRangeComparator.areDifferent(contentPartner.ageRange, upsertContentPartnerRequest.ageRange)) {
            commandCreator.updateAgeRange()?.let { updateFields.add(it) }
        }

        if (contentPartner.legalRestriction?.id?.value != upsertContentPartnerRequest.legalRestrictions?.id
        ) {
            commandCreator.updateLegalRestrictions(legalRestrictionsRepository)?.let { updateFields.add(it) }
        }

        if (contentPartner.distributionMethods != upsertContentPartnerRequest.distributionMethods) {
            commandCreator.updateHiddenDeliveryMethods()?.let { updateFields.add(it) }
        }

        if (contentPartner.remittance?.currency.toString() != upsertContentPartnerRequest.currency) {
            commandCreator.updateCurrency()?.let { updateFields.add(it) }
        }

        if (contentPartner.contentTypes != upsertContentPartnerRequest.contentTypes) {
            commandCreator.updateContentPartnerTypes()?.let { updateFields.add(it) }
        }

        if (contentPartner.contentCategories != upsertContentPartnerRequest.contentCategories) {
            commandCreator.updateContentContentCategories()?.let { updateFields.add(it) }
        }

        if (contentPartner.language?.toLanguageTag() != upsertContentPartnerRequest.language) {
            commandCreator.updateContentLanguage()?.let { updateFields.add(it) }
        }

        if (contentPartner.description != upsertContentPartnerRequest.description) {
            commandCreator.updateDescription()?.let { updateFields.add(it) }
        }

        if (contentPartner.awards != upsertContentPartnerRequest.awards) {
            commandCreator.updateAwards()?.let { updateFields.add(it) }
        }

        if (contentPartner.hubspotId != upsertContentPartnerRequest.hubspotId) {
            commandCreator.updateHubspotId()?.let { updateFields.add(it) }
        }

        if (contentPartner.notes != upsertContentPartnerRequest.notes) {
            commandCreator.updateNotes()?.let { updateFields.add(it) }
        }

        return updateFields
    }
}

class ContentPartnerUpdateCommandCreator(
    val id: ContentPartnerId,
    private val upsertContentPartnerRequest: UpsertContentPartnerRequest
) {

    fun updateHiddenDeliveryMethods(): ContentPartnerUpdateCommand? {
        return upsertContentPartnerRequest.distributionMethods
            ?.let {
                DistributionMethodResourceConverter.toDistributionMethods(it)
            }
            ?.let { deliveryMethods ->
                ReplaceDistributionMethods(contentPartnerId = id, distributionMethods = deliveryMethods)
            }
    }

    fun updateName(): ContentPartnerUpdateCommand.ReplaceName? =
        upsertContentPartnerRequest.name?.let {
            ContentPartnerUpdateCommand.ReplaceName(contentPartnerId = id, name = it)
        }

    fun updateAgeRange(): ContentPartnerUpdateCommand.ReplaceAgeRange? =
        upsertContentPartnerRequest.ageRange?.let {
            ContentPartnerUpdateCommand.ReplaceAgeRange(id, AgeRange.bounded(min = it.min, max = it.max))
        }

    fun updateLegalRestrictions(legalRestrictionsRepository: LegalRestrictionsRepository): ContentPartnerUpdateCommand.ReplaceLegalRestrictions? =
        upsertContentPartnerRequest.legalRestrictions
            ?.let { restrictionsRequest -> legalRestrictionsRepository.findById(LegalRestrictionsId(restrictionsRequest.id!!)) }
            ?.let { restrictions -> ContentPartnerUpdateCommand.ReplaceLegalRestrictions(id, restrictions) }

    fun updateCurrency(): ContentPartnerUpdateCommand.ReplaceCurrency? =
        upsertContentPartnerRequest.currency?.let {
            ContentPartnerUpdateCommand.ReplaceCurrency(id, Currency.getInstance(it))
        }

    fun updateContentPartnerTypes(): ContentPartnerUpdateCommand.ReplaceContentTypes? =
        upsertContentPartnerRequest.contentTypes?.let { contentTypes ->
            ContentPartnerUpdateCommand.ReplaceContentTypes(id, contentTypes)
        }

    fun updateContentContentCategories(): ContentPartnerUpdateCommand.ReplaceContentCategories? =
        upsertContentPartnerRequest.contentCategories?.let { contentCategories ->
            ContentPartnerUpdateCommand.ReplaceContentCategories(id, contentCategories)
        }

    fun updateContentLanguage(): ContentPartnerUpdateCommand.ReplaceLanguage? =
        upsertContentPartnerRequest.language?.let { language ->
            ContentPartnerUpdateCommand.ReplaceLanguage(id, language)
        }

    fun updateDescription(): ContentPartnerUpdateCommand.ReplaceDescription? =
        upsertContentPartnerRequest.description?.let { description ->
            ContentPartnerUpdateCommand.ReplaceDescription(id, description)
        }

    fun updateAwards(): ContentPartnerUpdateCommand.ReplaceAwards? =
        upsertContentPartnerRequest.awards?.let { awards ->
            ContentPartnerUpdateCommand.ReplaceAwards(id, awards)
        }

    fun updateHubspotId(): ContentPartnerUpdateCommand.ReplaceHubspotId? =
        upsertContentPartnerRequest.hubspotId?.let { hubspotId ->
            ContentPartnerUpdateCommand.ReplaceHubspotId(id, hubspotId)
        }

    fun updateNotes(): ContentPartnerUpdateCommand.ReplaceNotes? =
        upsertContentPartnerRequest.notes?.let { notes ->
            ContentPartnerUpdateCommand.ReplaceNotes(id, notes)
        }
}
