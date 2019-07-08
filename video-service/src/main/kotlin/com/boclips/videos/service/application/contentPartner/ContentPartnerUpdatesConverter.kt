package com.boclips.videos.service.application.contentPartner

import com.boclips.videos.service.domain.model.common.AgeRange
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerId
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerUpdateCommand
import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerUpdateCommand.SetHiddenDeliveryMethods
import com.boclips.videos.service.domain.model.video.DeliveryMethod
import com.boclips.videos.service.presentation.contentPartner.ContentPartnerRequest
import com.boclips.videos.service.presentation.deliveryMethod.DeliveryMethodResourceConverter

class ContentPartnerUpdatesConverter {
    fun convert(id: ContentPartnerId, contentPartnerRequest: ContentPartnerRequest): List<ContentPartnerUpdateCommand> {
        return listOfNotNull(
            updateNameOrNot(id, contentPartnerRequest),
            updateAgeRangeOrNot(id, contentPartnerRequest),
            updateSearchableOrNot(id, contentPartnerRequest),
            updateHiddenDeliveryMethodsOrNot(id, contentPartnerRequest)
        )
    }

    private fun updateHiddenDeliveryMethodsOrNot(
        id: ContentPartnerId,
        contentPartnerRequest: ContentPartnerRequest
    ): ContentPartnerUpdateCommand? =
        getDeliveryMethodsFromRequest(contentPartnerRequest)?.let { deliveryMethods ->
            SetHiddenDeliveryMethods(id, deliveryMethods)
        } ?: getDeliveryMethodsCommandFromSearchable(id, contentPartnerRequest)

    private fun updateSearchableOrNot(
        id: ContentPartnerId,
        contentPartnerRequest: ContentPartnerRequest
    ): ContentPartnerUpdateCommand.SetSearchability? =
        getDeliveryMethodsFromRequest(contentPartnerRequest)?.let { deliveryMethods ->
            ContentPartnerUpdateCommand.SetSearchability(id, deliveryMethods != DeliveryMethod.ALL)
        } ?: contentPartnerRequest.searchable?.let { searchable ->
            ContentPartnerUpdateCommand.SetSearchability(id, searchable)
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

    private fun getDeliveryMethodsFromRequest(request: ContentPartnerRequest) =
        request.hiddenFromSearchForDeliveryMethods?.map(DeliveryMethodResourceConverter::fromResource)?.toSet()

    private fun getDeliveryMethodsCommandFromSearchable(
        id: ContentPartnerId, contentPartnerRequest: ContentPartnerRequest
    ) =
        contentPartnerRequest.searchable?.let { searchable ->
            SetHiddenDeliveryMethods(
                id, if (searchable) {
                    emptySet()
                } else {
                    DeliveryMethod.ALL
                }
            )
        }
}
