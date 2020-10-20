package com.boclips.videos.service.testsupport

import com.boclips.videos.service.common.SuggestionsResults
import com.boclips.videos.service.domain.model.suggestions.ChannelSuggestion
import com.boclips.videos.service.domain.model.suggestions.SubjectSuggestion

class SuggestionFactory {
    companion object {
        fun create(
            channels: List<ChannelSuggestion>,
            subjects: List<SubjectSuggestion>
        ): SuggestionsResults {
            return SuggestionsResults(channels = channels, subjects = subjects)
        }
    }
}
