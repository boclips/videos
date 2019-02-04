package com.boclips.videos.service.application.event

import com.boclips.videos.service.infrastructure.event.EventService
import com.boclips.videos.service.infrastructure.event.EventsStatus

class CheckEventsStatus(private val eventService: EventService) {
    operator fun invoke(): EventsStatus {
        return eventService.status()
    }
}
