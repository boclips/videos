package com.boclips.contentpartner.service.domain.model.channel

import com.boclips.videos.api.common.IngestType

sealed class ChannelFilter {
    data class NameFilter(val name: String) : ChannelFilter()
    data class OfficialFilter(val official: Boolean) : ChannelFilter()
    data class AccreditedTo(val credit: Credit) : ChannelFilter()
    data class HubspotIdFilter(val hubspotId: String) : ChannelFilter()
    data class IngestTypesFilter(val ingestTypes: List<IngestType>) : ChannelFilter()
}