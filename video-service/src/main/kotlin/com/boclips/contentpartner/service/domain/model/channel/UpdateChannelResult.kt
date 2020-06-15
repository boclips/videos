package com.boclips.contentpartner.service.domain.model.channel

sealed class UpdateChannelResult {
    data class Success(val channel: Channel): UpdateChannelResult()
    data class ChannelNotFound(val channelId: ChannelId): UpdateChannelResult()
    data class MissingContract(val channelId: ChannelId): UpdateChannelResult()
}
