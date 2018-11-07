package com.boclips.videos.service.application.event

import com.boclips.videos.service.infrastructure.event.EventService
import com.boclips.videos.service.infrastructure.event.analysis.Interaction

class GetLatestInteractions(private val eventService: EventService) {
    fun execute(): List<Interaction> {
        return eventService.latestInteractions()
    }
}