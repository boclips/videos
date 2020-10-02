package com.boclips.videos.service.domain.model.suggestion

import com.boclips.contentpartner.service.domain.model.channel.ChannelId

data class Suggestion(
    val channelId: ChannelId,
    val name: String,
)
