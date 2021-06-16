package com.boclips.contentpartner.service.application.channel

import com.boclips.contentpartner.service.application.exceptions.ChannelNotFoundException
import com.boclips.contentpartner.service.domain.model.channel.ChannelId
import com.boclips.contentpartner.service.domain.model.channel.ChannelRepository
import com.boclips.eventbus.BoclipsEventListener
import com.boclips.eventbus.events.contentpartner.ChannelUpdateIndex
import com.boclips.videos.service.domain.service.suggestions.ChannelIndex

class UpdateChannelIndex(
    private val channelRepository: ChannelRepository,
    private val channelIndex: ChannelIndex
) {
    @BoclipsEventListener
    fun updateChannelIndex(channelUpdateIndex: ChannelUpdateIndex) {
        val contentPartner = channelUpdateIndex.channel
        val updatedChannel = channelRepository.findById(
            ChannelId(
                value = contentPartner.id.value
            )
        )
            ?: throw ChannelNotFoundException(
                channelId = ChannelId(
                    value = contentPartner.id.value
                )
            )

        channelIndex.upsert(sequenceOf(updatedChannel))
    }
}
