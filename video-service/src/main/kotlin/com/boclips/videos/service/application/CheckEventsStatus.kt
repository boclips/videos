package com.boclips.videos.service.application

import com.boclips.videos.service.infrastructure.event.EventService

class CheckEventsStatus(val eventService: EventService) {

    fun execute(): Boolean {
        return eventService.status()
    }


}
