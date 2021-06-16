package com.boclips.videos.service.application.channels

import com.boclips.contentpartner.service.domain.model.channel.ChannelFilter
import com.boclips.contentpartner.service.domain.model.channel.ChannelRepository
import com.boclips.contentpartner.service.domain.model.channel.Taxonomy
import com.boclips.videos.service.domain.model.video.channel.Channel
import com.boclips.videos.service.domain.model.video.channel.ChannelId
import com.boclips.videos.service.domain.model.video.channel.ChannelWithCategories
import com.boclips.videos.service.domain.model.video.channel.ContentPartnerAvailability
import org.springframework.stereotype.Component
import com.boclips.contentpartner.service.domain.model.channel.ChannelId as ContentPartnerServiceChannelId

@Component
class VideoChannelService(val channelRepository: ChannelRepository) {
    var idCache: Pair<ChannelId, ContentPartnerAvailability>? = null

    fun findChannelWithCategories(id: String): ChannelWithCategories? {
        return find(ChannelId(id))
            ?.let { channel ->
                ChannelWithCategories(
                    channel = Channel(channelId = ChannelId(value = channel.id.value), name = channel.name),
                    categories = (channel.taxonomy as? Taxonomy.ChannelLevelTagging)?.categories ?: emptySet()
                )
            }
    }

    fun findAllByIds(ids: List<ChannelId>): List<Channel> {
        val contentPartnerServiceChannelIds = ids.map { ContentPartnerServiceChannelId(value = it.value) }
        return channelRepository.findAllByIds(contentPartnerServiceChannelIds).map {
            Channel(channelId = ChannelId(it.id.value), name = it.name)
        }
    }

    fun findAvailabilityFor(channelId: ChannelId): ContentPartnerAvailability {
        idCache?.let {
            if (it.first == channelId) {
                return it.second
            }
        }

        val channel = channelRepository.findById(
            channelId = ContentPartnerServiceChannelId(
                value = channelId.value
            )
        )

        return (channel?.availability() ?: ContentPartnerAvailability.NONE)
            .also {
                idCache = channelId to it
            }
    }

    fun getPrivateChannelIDs(): Set<ChannelId> =
        channelRepository.findAll(listOf(ChannelFilter.PrivateFilter(private = true)))
            .map { ChannelId(it.id.value) }.toSet()

    private fun find(channelId: ChannelId): com.boclips.contentpartner.service.domain.model.channel.Channel? {
        return channelRepository.findById(
            ContentPartnerServiceChannelId(
                value = channelId.value
            )
        )
    }
}
