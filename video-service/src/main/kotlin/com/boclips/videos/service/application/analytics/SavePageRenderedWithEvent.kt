package com.boclips.videos.service.application.analytics

import com.boclips.videos.service.domain.service.events.EventService
import com.boclips.videos.service.presentation.event.PageRenderedWithEvent

class SavePageRenderedWithEvent(val eventService: EventService) {
    fun execute(event: PageRenderedWithEvent) {
        eventService.savePageRenderedWithEvent(url = event.url)
    }
}
