package com.boclips.videos.service.domain.service

import com.boclips.contentpartner.service.domain.model.channel.ChannelRepository
import com.boclips.videos.service.domain.model.video.channel.Availability
import com.boclips.videos.service.domain.model.video.channel.Channel
import com.boclips.videos.service.domain.model.video.channel.ChannelId
import org.springframework.stereotype.Component
import com.boclips.contentpartner.service.domain.model.channel.ChannelId as ContentPartnerServiceChannelId

@Component
class VideoChannelService(val channelRepository: ChannelRepository) {
    var idCache: Pair<ChannelId, Availability>? = null

    fun findById(id: String): Channel? {
        return find(ChannelId(id))
            ?.let {
                Channel(
                    channelId = ChannelId(value = it.id.value),
                    name = it.name
                )
            }
    }

    fun findAvailabilityFor(channelId: ChannelId): Availability {
        idCache?.let {
            if (it.first == channelId) {
                return it.second
            }
        }

        return (channelRepository.findById(
            channelId = ContentPartnerServiceChannelId(
                value = channelId.value
            )
        )?.let {
            when {
                it.isDownloadable() && it.isStreamable() -> Availability.ALL
                it.isDownloadable() -> Availability.DOWNLOAD
                it.isStreamable() -> Availability.STREAMING
                else -> Availability.NONE
            }
        } ?: Availability.NONE)
            .also {
                idCache = channelId to it
            }
    }

    private fun find(channelId: ChannelId): com.boclips.contentpartner.service.domain.model.channel.Channel? {
        return channelRepository.findById(
            ContentPartnerServiceChannelId(
                value = channelId.value
            )
        )
    }
}