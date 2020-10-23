package com.boclips.videos.service.infrastructure.search

import com.boclips.search.service.domain.channels.model.ChannelMetadata
import com.boclips.search.service.domain.channels.model.ContentType
import com.boclips.videos.service.domain.model.suggestions.ChannelSuggestion

object ChannelMetadataConverter {
    fun convert(channelSuggestion: ChannelSuggestion): ChannelMetadata {
        return ChannelMetadata(
            id = channelSuggestion.id.value,
            name = channelSuggestion.name,
            eligibleForStream = channelSuggestion.eligibleForStream,
            contentTypes = channelSuggestion.contentTypes.map {
                when (it) {
                    com.boclips.contentpartner.service.domain.model.channel.ContentType.NEWS -> ContentType.NEWS
                    com.boclips.contentpartner.service.domain.model.channel.ContentType.STOCK -> ContentType.STOCK
                    com.boclips.contentpartner.service.domain.model.channel.ContentType.INSTRUCTIONAL -> ContentType.INSTRUCTIONAL
                }
            }
        )
    }
}
