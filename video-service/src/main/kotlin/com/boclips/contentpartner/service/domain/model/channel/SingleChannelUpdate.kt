package com.boclips.contentpartner.service.domain.model.channel

data class SingleChannelUpdate(val id: ChannelId, val updateCommands: List<ChannelUpdateCommand>) {
    inline fun <reified R : ChannelUpdateCommand> getUpdateByType(): R? {
        return updateCommands.filterIsInstance<R>().firstOrNull()
    }
}
