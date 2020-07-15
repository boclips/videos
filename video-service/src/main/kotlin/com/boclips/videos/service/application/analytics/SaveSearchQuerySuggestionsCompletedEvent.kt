package com.boclips.videos.service.application.analytics

import com.boclips.videos.service.domain.model.user.User
import com.boclips.videos.service.domain.service.events.EventService
import com.boclips.videos.service.presentation.event.SearchQuerySuggestionsCompletedEvent

class SaveSearchQuerySuggestionsCompletedEvent(private val eventService: EventService) {
    fun execute(event: SearchQuerySuggestionsCompletedEvent?, user: User) {
        event ?: throw InvalidEventException("SearchQuerySuggestionsCompletedEvent must not be null")
        event.isValidOrThrows()

        eventService.saveSearchQueryCompletionsSuggestedEvent(
            searchQuery = event.searchQuery,
            impressions = event.impressions,
            componentId = event.componentId,
            completionId = event.completionId,
            user = user
        )
    }
}
