package com.boclips.contentpartner.service.domain.model.channel

import com.boclips.videos.api.common.IngestType

sealed class ChannelFilter {
    data class NameFilter(val name: String) : ChannelFilter()
    data class IngestTypesFilter(val ingestTypes: List<IngestType>) : ChannelFilter()
    data class HiddenFilter(val hidden: Boolean) : ChannelFilter()
}
