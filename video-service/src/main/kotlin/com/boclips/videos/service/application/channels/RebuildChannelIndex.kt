package com.boclips.videos.service.application.channels

import com.boclips.contentpartner.service.domain.model.channel.ChannelRepository
import com.boclips.search.service.domain.common.ProgressNotifier
import com.boclips.videos.service.domain.service.suggestions.ChannelIndex
import mu.KLogging

open class RebuildChannelIndex(
    private val channelRepository: ChannelRepository,
    private val channelsIndex: ChannelIndex
) {
    companion object : KLogging()

    open operator fun invoke(notifier: ProgressNotifier? = null) {
        logger.info("Starting a full reindex")

        channelRepository.streamAll { channels ->
            channelsIndex.safeRebuildIndex(channels, notifier)
        }

        logger.info("Full reindex done")
    }
}
