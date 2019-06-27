package com.boclips.videos.service.application.contentPartner

import com.boclips.videos.service.domain.model.common.AgeRange
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerId
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerUpdateCommand
import com.boclips.videos.service.presentation.contentPartner.ContentPartnerRequest

class ContentPartnerUpdatesConverter {
    fun convert(id: ContentPartnerId, contentPartnerRequest: ContentPartnerRequest): List<ContentPartnerUpdateCommand> {
        return listOfNotNull(
            updateNameOrNot(id, contentPartnerRequest),
            updateAgeRangeOrNot(id, contentPartnerRequest),
            updateSearchableOrNot(id, contentPartnerRequest)
        )
    }

    private fun updateSearchableOrNot(
        id: ContentPartnerId,
        contentPartnerRequest: ContentPartnerRequest
    ): ContentPartnerUpdateCommand.SetSearchability? =
        contentPartnerRequest.searchable?.let {
            ContentPartnerUpdateCommand.SetSearchability(id, it)
        }

    private fun updateNameOrNot(
        id: ContentPartnerId,
        contentPartnerRequest: ContentPartnerRequest
    ): ContentPartnerUpdateCommand.ReplaceName? =
        contentPartnerRequest.name?.let {
            ContentPartnerUpdateCommand.ReplaceName(
                id,
                it
            )
        }

    private fun updateAgeRangeOrNot(
        id: ContentPartnerId, contentPartnerRequest: ContentPartnerRequest
    ): ContentPartnerUpdateCommand.ReplaceAgeRange? =
        contentPartnerRequest.ageRange?.let {
            ContentPartnerUpdateCommand.ReplaceAgeRange(id, AgeRange.bounded(min = it.min, max = it.max))
        }
}