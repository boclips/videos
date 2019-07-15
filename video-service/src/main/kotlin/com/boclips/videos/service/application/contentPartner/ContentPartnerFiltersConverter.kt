package com.boclips.videos.service.application.contentPartner

import com.boclips.videos.service.domain.model.contentPartner.ContentPartnerFilter
import com.boclips.videos.service.domain.model.contentPartner.Credit

object ContentPartnerFiltersConverter {
    fun convert(name: String? = null, isOfficial: Boolean? = null, accreditedYTChannelId: String?): List<ContentPartnerFilter> =
        listOfNotNull(
            getNameFilter(name),
            getOfficialFilter(isOfficial),
            getAccreditedToFilter(accreditedYTChannelId)
        )

    private fun getNameFilter(name: String?) =
        name?.let { ContentPartnerFilter.NameFilter(name = it) }

    private fun getOfficialFilter(isOfficial: Boolean?) =
        isOfficial?.let { ContentPartnerFilter.OfficialFilter(isOfficial = it) }

    private fun getAccreditedToFilter(accreditedYTChannelId: String?) =
        accreditedYTChannelId?.let { ContentPartnerFilter.AccreditedTo(credit = Credit.YoutubeCredit(channelId = accreditedYTChannelId)) }

}
