package com.boclips.videos.service.presentation

import com.boclips.videos.service.application.event.CheckEventsStatus
import com.boclips.videos.service.application.event.CreateEvent
import com.boclips.videos.service.application.event.GetLatestInteractions
import com.boclips.videos.service.infrastructure.event.EventsStatus
import com.boclips.videos.service.presentation.event.CreateNoSearchResultsEventCommand
import com.boclips.videos.service.presentation.event.CreatePlaybackEventCommand
import com.boclips.videos.service.presentation.event.NoSearchResultsEventResource
import com.boclips.videos.service.presentation.event.convertToResource
import org.springframework.hateoas.Resources
import org.springframework.hateoas.mvc.ControllerLinkBuilder
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/events")
class EventController(
        private val createEvent: CreateEvent,
        private val checkEventsStatus: CheckEventsStatus,
        private val getLatestInteractions: GetLatestInteractions
) {
    companion object {
        fun createPlaybackEventLink() = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(EventController::class.java).logPlaybackEvent(null)).withRel("createPlaybackEvent")
        fun createNoResultsEventLink() = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(EventController::class.java).logNoSearchResultsEvent(null)).withRel("createNoSearchResultsEvent")
    }

    @PostMapping("/playback")
    fun logPlaybackEvent(@RequestBody playbackEvent: CreatePlaybackEventCommand?): ResponseEntity<Void> {
        createEvent.createPlaybackEvent(playbackEvent)
        return ResponseEntity(HttpStatus.CREATED)
    }

    @PostMapping("/no-search-results")
    fun logNoSearchResultsEvent(@RequestBody noSearchResultsEvent: CreateNoSearchResultsEventCommand?): ResponseEntity<Void> {
        createEvent.createNoSearchResultsEvent(noSearchResultsEvent)
        return ResponseEntity(HttpStatus.CREATED)
    }

    @GetMapping("/no-search-results")
    fun getNoSearchResultsEvent(): ResponseEntity<Resources<NoSearchResultsEventResource>> {
        val events = getLatestInteractions.getAllNoSearchResultEvents()
                .map { convertToResource(it) }

        return ResponseEntity(Resources(events), HttpStatus.OK)
    }

    @GetMapping("/status")
    fun status(): ResponseEntity<EventsStatus> {
        val status = checkEventsStatus.execute()
        val code = if (status.healthy) HttpStatus.OK else HttpStatus.SERVICE_UNAVAILABLE

        return ResponseEntity(status, code)
    }
}
