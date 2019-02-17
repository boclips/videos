package com.boclips.videos.service.presentation

import com.boclips.videos.service.application.event.SavePlaybackEvent
import com.boclips.videos.service.application.video.search.ReportNoResults
import com.boclips.videos.service.presentation.event.CreateNoSearchResultsEventCommand
import com.boclips.videos.service.presentation.event.CreatePlaybackEventCommand
import org.springframework.hateoas.mvc.ControllerLinkBuilder
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/events")
class EventController(
    private val savePlaybackEvent: SavePlaybackEvent,
    private val reportNoResults: ReportNoResults
) {
    companion object {
        fun createPlaybackEventLink() = ControllerLinkBuilder.linkTo(
            ControllerLinkBuilder.methodOn(EventController::class.java).logPlaybackEvent(null)
        ).withRel("createPlaybackEvent")

        fun createNoResultsEventLink() = ControllerLinkBuilder.linkTo(
            ControllerLinkBuilder.methodOn(EventController::class.java).logNoSearchResultsEvent(null)
        ).withRel("createNoSearchResultsEvent")
    }

    @PostMapping("/playback")
    fun logPlaybackEvent(@RequestBody playbackEvent: CreatePlaybackEventCommand?): ResponseEntity<Void> {
        savePlaybackEvent.execute(playbackEvent)
        return ResponseEntity(HttpStatus.CREATED)
    }

    @PostMapping("/no-search-results")
    fun logNoSearchResultsEvent(@RequestBody noSearchResultsEvent: CreateNoSearchResultsEventCommand?): ResponseEntity<Void> {
        reportNoResults.execute(noSearchResultsEvent)
        return ResponseEntity(HttpStatus.CREATED)
    }
}
