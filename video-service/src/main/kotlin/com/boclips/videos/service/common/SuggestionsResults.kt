package com.boclips.videos.service.common

import com.boclips.videos.service.domain.model.suggestions.ChannelSuggestion
import com.boclips.videos.service.domain.model.suggestions.SubjectSuggestion

data class SuggestionsResults(val channels: Iterable<ChannelSuggestion>, val subjects: Iterable<SubjectSuggestion>)
