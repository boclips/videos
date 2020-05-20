package com.boclips.contentpartner.service.application.channel

import com.boclips.contentpartner.service.domain.model.channel.Channel
import com.boclips.contentpartner.service.domain.model.channel.ChannelId
import com.boclips.contentpartner.service.domain.model.channel.ChannelRepository
import com.boclips.web.exceptions.ResourceNotFoundApiException

class GetChannel(
    private val channelRepository: ChannelRepository
) {
    operator fun invoke(contentPartnerId: String): Channel {
        return channelRepository.findById(
            ChannelId(value = contentPartnerId)
        )
            ?: throw ResourceNotFoundApiException(
                error = "Content partner not found",
                message = "No content partner found for this id: $contentPartnerId"
            )
    }
}
