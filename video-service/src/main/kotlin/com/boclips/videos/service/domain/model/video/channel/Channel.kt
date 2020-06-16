package com.boclips.videos.service.domain.model.video.channel

data class Channel(
    val channelId: ChannelId,
    val name: String
) {
    override fun toString(): String {
        return "Channel(id = ${this.channelId.value}, name = ${this.name})"
    }
}
