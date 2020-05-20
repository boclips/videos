package com.boclips.contentpartner.service.application.channel

import com.boclips.contentpartner.service.domain.model.channel.ChannelFilter
import com.boclips.contentpartner.service.domain.model.channel.Credit
import com.boclips.videos.api.common.IngestType

object ChannelFiltersConverter {
    fun convert(
        name: String? = null,
        official: Boolean? = null,
        accreditedYTChannelId: String?,
        hubspotId: String? = null,
        ingestTypes: List<IngestType>? = null
    ): List<ChannelFilter> =
        listOfNotNull(
            getNameFilter(
                name
            ),
            getOfficialFilter(
                official
            ),
            getAccreditedToFilter(
                accreditedYTChannelId
            ),
            getHubspotId(
                hubspotId
            ),
            getIngestTypesFilter(
                ingestTypes
            )
        )

    private fun getNameFilter(name: String?) =
        name?.let { ChannelFilter.NameFilter(name = it) }

    private fun getOfficialFilter(official: Boolean?) =
        official?.let { ChannelFilter.OfficialFilter(official = it) }

    private fun getAccreditedToFilter(accreditedYTChannelId: String?) =
        accreditedYTChannelId?.let { ChannelFilter.AccreditedTo(credit = Credit.YoutubeCredit(channelId = accreditedYTChannelId)) }

    private fun getHubspotId(hubspotId: String?) =
        hubspotId?.let { ChannelFilter.HubspotIdFilter(hubspotId = hubspotId) }

    private fun getIngestTypesFilter(ingestTypes: List<IngestType>?) =
        ingestTypes?.let { ChannelFilter.IngestTypesFilter(ingestTypes = it) }
}
