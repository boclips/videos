package com.boclips.videos.service.infrastructure.video.converters

import com.boclips.videos.service.domain.model.video.channel.Channel
import com.boclips.videos.service.domain.model.video.channel.ChannelId
import com.boclips.videos.service.infrastructure.video.ChannelDocument
import org.bson.types.ObjectId

object ChannelDocumentConverter {
    fun toChannelDocument(channel: Channel): ChannelDocument {
        return ChannelDocument(
            id = ObjectId(channel.channelId.value),
            name = channel.name
        )
    }

    fun toChannel(document: ChannelDocument): Channel {
        return Channel(
            channelId = ChannelId(
                value = document.id.toString()
            ),
            name = document.name
        )
    }
}
