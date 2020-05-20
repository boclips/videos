package com.boclips.contentpartner.service.application.channel

import com.boclips.contentpartner.service.domain.model.channel.Channel
import com.boclips.contentpartner.service.domain.model.channel.ChannelRepository
import com.boclips.videos.api.common.IngestType

class GetChannels(private val channelRepository: ChannelRepository) {
    operator fun invoke(
        name: String? = null,
        official: Boolean? = null,
        accreditedToYtChannelId: String? = null,
        ingestTypes: List<IngestType>? = null
    ): Iterable<Channel> {
        val filters =
            ChannelFiltersConverter.convert(
                name = name,
                official = official,
                accreditedYTChannelId = accreditedToYtChannelId,
                ingestTypes = ingestTypes
            )

        return channelRepository.findAll(filters)
    }
}
