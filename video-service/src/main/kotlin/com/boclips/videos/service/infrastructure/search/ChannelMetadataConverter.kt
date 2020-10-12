package com.boclips.videos.service.infrastructure.search

import com.boclips.search.service.domain.channels.model.ChannelMetadata
import com.boclips.videos.service.domain.model.suggestions.ChannelSuggestion

object ChannelMetadataConverter {
    fun convert(channelSuggestion: ChannelSuggestion): ChannelMetadata {
        return ChannelMetadata(
            id = channelSuggestion.id.value,
            name = channelSuggestion.name
        )
    }
}
