package com.boclips.contentpartner.service.application.channel

import com.boclips.contentpartner.service.domain.model.channel.ChannelFilter
import com.boclips.videos.api.common.IngestType

object ChannelFiltersConverter {
    fun convert(
        name: String? = null,
        hubspotId: String? = null,
        ingestTypes: List<IngestType>? = null
    ): List<ChannelFilter> =
        listOfNotNull(
            getNameFilter(
                name
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

    private fun getHubspotId(hubspotId: String?) =
        hubspotId?.let { ChannelFilter.HubspotIdFilter(hubspotId = hubspotId) }

    private fun getIngestTypesFilter(ingestTypes: List<IngestType>?) =
        ingestTypes?.let { ChannelFilter.IngestTypesFilter(ingestTypes = it) }
}
