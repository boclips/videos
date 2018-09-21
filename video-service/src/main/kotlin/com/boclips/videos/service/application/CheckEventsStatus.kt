package com.boclips.videos.service.application

import com.boclips.videos.service.infrastructure.event.EventService
import com.boclips.videos.service.infrastructure.event.EventsStatus

class CheckEventsStatus(private val eventService: EventService) {

    fun execute(): EventsStatus {
        return eventService.status()
    }
}
