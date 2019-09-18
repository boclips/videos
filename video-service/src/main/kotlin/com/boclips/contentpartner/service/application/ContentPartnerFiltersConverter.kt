package com.boclips.contentpartner.service.application

import com.boclips.contentpartner.service.domain.model.ContentPartnerFilter
import com.boclips.contentpartner.service.domain.model.Credit

object ContentPartnerFiltersConverter {
    fun convert(
        name: String? = null,
        official: Boolean? = null,
        accreditedYTChannelId: String?
    ): List<ContentPartnerFilter> =
        listOfNotNull(
            getNameFilter(name),
            getOfficialFilter(official),
            getAccreditedToFilter(
                accreditedYTChannelId
            )
        )

    private fun getNameFilter(name: String?) =
        name?.let { ContentPartnerFilter.NameFilter(name = it) }

    private fun getOfficialFilter(official: Boolean?) =
        official?.let { ContentPartnerFilter.OfficialFilter(official = it) }

    private fun getAccreditedToFilter(accreditedYTChannelId: String?) =
        accreditedYTChannelId?.let { ContentPartnerFilter.AccreditedTo(credit = Credit.YoutubeCredit(channelId = accreditedYTChannelId)) }
}
