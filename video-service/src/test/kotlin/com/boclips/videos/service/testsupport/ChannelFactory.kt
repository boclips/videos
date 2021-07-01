package com.boclips.videos.service.testsupport

import com.boclips.videos.service.domain.model.suggestions.ChannelSuggestion
import com.boclips.videos.service.domain.model.video.channel.Channel
import com.boclips.videos.service.domain.model.video.channel.ChannelId
import com.boclips.contentpartner.service.domain.model.channel.ChannelId as ContentPartnerId

class ChannelFactory {
    companion object {
        fun create(id: String, name: String): Channel {
            return Channel(
                channelId = ChannelId(id),
                name = name,
            )
        }

        fun createSuggestion(id: String, name: String): ChannelSuggestion {
            return ChannelSuggestion(
                id = ContentPartnerId(id),
                name = name
            )
        }
    }
}
