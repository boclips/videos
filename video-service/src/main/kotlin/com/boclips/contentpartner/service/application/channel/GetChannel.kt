package com.boclips.contentpartner.service.application.channel

import com.boclips.contentpartner.service.domain.model.channel.Channel
import com.boclips.contentpartner.service.domain.model.channel.ChannelId
import com.boclips.contentpartner.service.domain.model.channel.ChannelRepository
import com.boclips.web.exceptions.ResourceNotFoundApiException

class GetChannel(
    private val channelRepository: ChannelRepository
) {
    operator fun invoke(channelId: String): Channel {
        return channelRepository.findById(
            ChannelId(value = channelId)
        )
            ?: throw ResourceNotFoundApiException(
                error = "Channel not found",
                message = "No channel found for this id: $channelId"
            )
    }
}
