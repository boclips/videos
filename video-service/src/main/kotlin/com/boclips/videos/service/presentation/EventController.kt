package com.boclips.videos.service.presentation

import com.boclips.videos.service.application.CheckEventsStatus
import com.boclips.videos.service.application.CreateEvent
import org.springframework.hateoas.mvc.ControllerLinkBuilder
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/events")
class EventController(
        private val createEvent: CreateEvent,
        private val checkEventsStatus: CheckEventsStatus
) {
    companion object {
        fun createEventLink() = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(EventController::class.java).logEvent(null)).withRel("createEvent")
    }

    @PostMapping
    fun logEvent(@RequestBody playbackEvent: CreatePlaybackEventCommand?): ResponseEntity<Void> {
        createEvent.execute(playbackEvent)
        return ResponseEntity(HttpStatus.CREATED)
    }

    @GetMapping("/status")
    fun status(): ResponseEntity<String> {
        if(!checkEventsStatus.execute()) {
            return ResponseEntity("Something wrong with the events", HttpStatus.INTERNAL_SERVER_ERROR)
        }
        return ResponseEntity("OK", HttpStatus.OK)
    }
}
