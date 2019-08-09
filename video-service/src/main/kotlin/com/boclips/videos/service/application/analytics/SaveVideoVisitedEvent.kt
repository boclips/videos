package com.boclips.videos.service.application.analytics

import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.events.EventService
import com.boclips.videos.service.presentation.event.CreateVideoVisitedEventCommand

class SaveVideoVisitedEvent(
    private val eventService: EventService
) {
    fun execute(command: CreateVideoVisitedEventCommand?) {
        command?.videoId ?: throw InvalidEventException("Command must not be null")
        eventService.saveVideoVisitedEvent(VideoId(command.videoId))
    }

}
