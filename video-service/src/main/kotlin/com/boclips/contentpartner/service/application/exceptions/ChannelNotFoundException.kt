package com.boclips.contentpartner.service.application.exceptions

import com.boclips.contentpartner.service.domain.model.channel.ChannelId
import com.boclips.web.exceptions.ResourceNotFoundApiException

class ChannelNotFoundException(channelId: ChannelId) : ResourceNotFoundApiException(
    error = "Channel not found",
    message = "Could not find channel: $channelId"
)
