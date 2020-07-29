package com.boclips.videos.service.testsupport

import com.boclips.videos.service.domain.model.Suggestions
import com.boclips.videos.service.domain.model.subject.Subject

class SuggestionFactory {
    companion object {
        fun create(
            channels: List<String> = listOf("Ted", "Crash Course Biology"),
            subjects: List<Subject> = emptyList()
        ): Suggestions {
            return Suggestions(channels = channels, subjects = subjects)
        }
    }
}
