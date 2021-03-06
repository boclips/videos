package com.boclips.contentpartner.service.domain.model.channel

sealed class CreateChannelResult {
    data class Success(val channel: Channel) : CreateChannelResult()
    data class NameConflict(val name: String) : CreateChannelResult()
    object MissingContract : CreateChannelResult()
}
