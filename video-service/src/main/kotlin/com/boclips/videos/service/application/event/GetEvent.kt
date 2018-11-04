package com.boclips.videos.service.application.event

import com.boclips.videos.service.infrastructure.event.EventService
import com.boclips.videos.service.infrastructure.event.types.NoSearchResultsEvent

class GetEvent(
        private val eventService: EventService
) {
    fun execute(): List<NoSearchResultsEvent> {
        return eventService.getNoSearchResultsEvents()
    }
}