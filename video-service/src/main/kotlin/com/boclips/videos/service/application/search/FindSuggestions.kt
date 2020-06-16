package com.boclips.videos.service.application.search

import com.boclips.contentpartner.service.domain.model.channel.ChannelRepository
import com.boclips.videos.service.domain.model.Suggestions
import mu.KLogging

class FindSuggestions(private val channelRepository: ChannelRepository) {
    companion object : KLogging()

    operator fun invoke(query: String): Suggestions {
        val contentPartners = channelRepository.findByName(query)
            .take(10)
            .map { it.name }

        return Suggestions(channels = contentPartners)
    }
}
