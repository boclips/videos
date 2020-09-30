package com.boclips.videos.service.testsupport

import com.boclips.contentpartner.service.domain.model.channel.Channel
import com.boclips.videos.service.domain.model.Suggestions
import com.boclips.videos.service.domain.model.subject.Subject

class SuggestionFactory {
    companion object {
        fun create(
            channels: List<Channel>,
            subjects: List<Subject>
        ): Suggestions {
            return Suggestions(channels = channels, subjects = subjects)
        }
    }
}
