package com.boclips.videos.service.application.event

import com.boclips.videos.service.infrastructure.event.EventService
import com.boclips.videos.service.infrastructure.event.types.NoSearchResultsEvent

class GetEvent(
    private val eventService: EventService
) {
    operator fun invoke(): List<NoSearchResultsEvent> {
        return eventService.getNoSearchResultsEvents()
    }
}