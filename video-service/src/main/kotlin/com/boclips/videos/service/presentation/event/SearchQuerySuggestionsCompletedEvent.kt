package com.boclips.videos.service.presentation.event

import com.boclips.videos.service.application.analytics.InvalidEventException

data class SearchQuerySuggestionsCompletedEvent(
    val searchQuery: String,
    val impressions: List<String>,
    val componentId: String,
    val completionId: String
): EventCommand() {
    override fun isValidOrThrows() {
        if (this.searchQuery.isNullOrEmpty()) throw InvalidEventException("searchQuery must be specified")
        if (this.impressions.isNullOrEmpty()) throw InvalidEventException("impressions must be specified")
        if (this.componentId.isNullOrEmpty()) throw InvalidEventException("componentId must be specified")
        if (this.completionId.isNullOrEmpty()) throw InvalidEventException("completionId must be specified")
    }
}
