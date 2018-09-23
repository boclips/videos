package com.boclips.videos.service.application.event

import com.boclips.videos.service.infrastructure.event.EventService
import com.boclips.videos.service.infrastructure.event.EventsStatus

class CheckEventsStatus(private val eventService: EventService) {

    fun execute(): EventsStatus {
        return eventService.status()
    }
}
