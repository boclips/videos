package com.boclips.videos.service.domain.model.suggestions

import com.boclips.contentpartner.service.domain.model.channel.ChannelId

data class NewSuggestions(
    val channels: List<ChannelSuggestion>
)

data class ChannelSuggestion(
    val name: String,
    val id: ChannelId
)
