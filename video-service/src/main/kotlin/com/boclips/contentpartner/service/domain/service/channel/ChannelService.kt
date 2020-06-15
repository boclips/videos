package com.boclips.contentpartner.service.domain.service.channel

import com.boclips.contentpartner.service.application.channel.ChannelFiltersConverter
import com.boclips.contentpartner.service.domain.model.channel.Channel
import com.boclips.contentpartner.service.domain.model.channel.ChannelRepository
import com.boclips.contentpartner.service.domain.model.channel.ChannelUpdateCommand
import com.boclips.contentpartner.service.domain.model.channel.CreateChannelResult
import com.boclips.contentpartner.service.domain.model.channel.SingleChannelUpdate
import com.boclips.contentpartner.service.domain.model.channel.UpdateChannelResult
import com.boclips.videos.api.common.IngestType

class ChannelService(
    private val channelRepository: ChannelRepository
) {
    fun create(channel: Channel): CreateChannelResult {
        if (hasNameConflict(channel)) {
            return CreateChannelResult.NameConflict(channel.name)
        }

        if (channel.hubspotId != null && hasHubSpotIdConflict(channel)) {
            return CreateChannelResult.HubSpotIdConflict(channel.hubspotId)
        }

        if (isMissingContract(channel)) {
            return CreateChannelResult.MissingContract
        }

        return CreateChannelResult.Success(channelRepository.create(channel))
    }

    fun update(update: SingleChannelUpdate): UpdateChannelResult {
        if (isMissingContract(update)) {
            return UpdateChannelResult.MissingContract(update.id)
        }

        channelRepository.update(update.updateCommands)

        val channel = channelRepository.findById(update.id)

        return if (channel != null) {
            UpdateChannelResult.Success(channel = channel)
        } else {
            UpdateChannelResult.ChannelNotFound(channelId = update.id)
        }
    }

    private fun isMissingContract(channel: Channel) =
        channel.ingest.type() != IngestType.YOUTUBE && channel.contract == null

    private fun hasHubSpotIdConflict(channel: Channel): Boolean =
        channelRepository.findAll(ChannelFiltersConverter.convert(hubspotId = channel.hubspotId)).toList().isNotEmpty()

    private fun hasNameConflict(channel: Channel): Boolean =
        channelRepository.findAll(ChannelFiltersConverter.convert(name = channel.name)).toList().isNotEmpty()


    private fun isMissingContract(update: SingleChannelUpdate): Boolean {
        update.getUpdateByType<ChannelUpdateCommand.ReplaceIngestDetails>()
            ?.takeIf { it.ingest.type() != IngestType.YOUTUBE }
            ?.let {
                val channel = channelRepository.findById(update.id)
                if (channel?.contract == null && update.getUpdateByType<ChannelUpdateCommand.ReplaceContract>() == null) {
                    return true
                }
            }

        return false
    }
}
