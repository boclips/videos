package com.boclips.videos.service.presentation

import com.boclips.videos.service.application.CreateEvent
import org.springframework.hateoas.mvc.ControllerLinkBuilder
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/events")
class EventController(private val createEvent: CreateEvent) {
    companion object {
        fun createEventLink() = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(EventController::class.java).logEvent(null)).withRel("createEvent")
    }

    @PostMapping
    fun logEvent(@RequestBody playbackEvent: PlaybackEvent?): ResponseEntity<Void> {
        createEvent.execute(playbackEvent)
        return ResponseEntity(HttpStatus.CREATED)
    }
}