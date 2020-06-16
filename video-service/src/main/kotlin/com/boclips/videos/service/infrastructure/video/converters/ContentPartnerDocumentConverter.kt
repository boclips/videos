package com.boclips.videos.service.infrastructure.video.converters

import com.boclips.videos.service.domain.model.video.channel.Channel
import com.boclips.videos.service.domain.model.video.channel.ChannelId
import com.boclips.videos.service.infrastructure.video.ContentPartnerDocument
import org.bson.types.ObjectId

object ContentPartnerDocumentConverter {
    fun toContentPartnerDocument(channel: Channel): ContentPartnerDocument {
        return ContentPartnerDocument(
            id = ObjectId(channel.channelId.value),
            name = channel.name
        )
    }

    fun toContentPartner(document: ContentPartnerDocument): Channel {
        return Channel(
            channelId = ChannelId(
                value = document.id.toString()
            ),
            name = document.name
        )
    }
}
