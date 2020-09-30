package com.boclips.videos.service.application.search

import com.boclips.contentpartner.service.domain.model.channel.ChannelRepository
import com.boclips.videos.service.domain.model.Suggestions
import com.boclips.videos.service.domain.service.subject.SubjectRepository
import mu.KLogging

class FindSuggestions(
    private val channelRepository: ChannelRepository,
    private val subjectRepository: SubjectRepository
) {
    companion object : KLogging()

    operator fun invoke(query: String): Suggestions {
        val channels = channelRepository.findByName(query)
            .take(10)

        val subjects = subjectRepository.findByQuery(query)
            .take(5)

        return Suggestions(channels = channels, subjects = subjects)
    }
}
