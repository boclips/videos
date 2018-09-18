package com.boclips.videos.service.presentation

import com.boclips.videos.service.application.CreateEvent
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/events")
class EventController(val createEvent: CreateEvent) {

    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    fun logEvent(@RequestBody playbackEvent: PlaybackEvent) {
        createEvent.execute(playbackEvent)
    }
}