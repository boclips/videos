package com.boclips.videos.service.presentation

import com.boclips.videos.service.application.event.CheckEventsStatus
import com.boclips.videos.service.application.event.CreateEvent
import com.boclips.videos.service.infrastructure.event.EventsStatus
import com.boclips.videos.service.application.event.CreatePlaybackEventCommand
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
    fun status(): ResponseEntity<EventsStatus> {

        val status = checkEventsStatus.execute()

        val code = if(status.healthy) HttpStatus.OK else HttpStatus.SERVICE_UNAVAILABLE

        return ResponseEntity(status, code)
    }
}
