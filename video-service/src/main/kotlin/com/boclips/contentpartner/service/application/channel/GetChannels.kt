package com.boclips.contentpartner.service.application.channel

import com.boclips.contentpartner.service.domain.model.channel.Channel
import com.boclips.contentpartner.service.domain.model.channel.ChannelRepository
import com.boclips.videos.api.common.IngestType

class GetChannels(private val channelRepository: ChannelRepository) {
    operator fun invoke(
        name: String? = null,
        ingestTypes: List<IngestType>? = null
    ): Iterable<Channel> {
        val filters =
            ChannelFiltersConverter.convert(
                name = name,
                ingestTypes = ingestTypes
            )

        return channelRepository.findAll(filters)
    }
}
