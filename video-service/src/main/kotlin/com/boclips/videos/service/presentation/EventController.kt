package com.boclips.videos.service.presentation

import com.boclips.videos.service.application.analytics.SavePlaybackEvent
import com.boclips.videos.service.application.analytics.SavePlayerInteractedWithEvent
import com.boclips.videos.service.application.analytics.SaveVideoInteractedWithEvent
import com.boclips.videos.service.application.video.search.ReportNoResults
import com.boclips.videos.service.presentation.event.CreateNoSearchResultsEventCommand
import com.boclips.videos.service.presentation.event.CreatePlaybackEventCommand
import com.boclips.videos.service.presentation.event.CreatePlayerInteractedWithEvent
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class EventController(
    private val savePlaybackEvent: SavePlaybackEvent,
    private val savePlayerInteractedWithEvent: SavePlayerInteractedWithEvent,
    private val saveVideoInteractedWithEvent: SaveVideoInteractedWithEvent,
    private val reportNoResults: ReportNoResults
) {

    @PostMapping("/v1/events/playback")
    fun logPlaybackEvent(@RequestBody playbackEvent: CreatePlaybackEventCommand?, @CookieValue(Cookies.PLAYBACK_DEVICE) playbackDevice: String? = null): ResponseEntity<Void> {
        savePlaybackEvent.execute(playbackEvent, playbackDevice)
        return ResponseEntity(HttpStatus.CREATED)
    }

    @PostMapping("/v1/events/player-interaction")
    fun logPlayerInteractedWithEvent(@RequestBody playbackEvent: CreatePlayerInteractedWithEvent?): ResponseEntity<Void> {
        savePlayerInteractedWithEvent.execute(playbackEvent)
        return ResponseEntity(HttpStatus.CREATED)
    }

    @PostMapping("/v1/events/no-search-results")
    fun logNoSearchResultsEvent(@RequestBody noSearchResultsEvent: CreateNoSearchResultsEventCommand?): ResponseEntity<Void> {
        reportNoResults.execute(noSearchResultsEvent)
        return ResponseEntity(HttpStatus.CREATED)
    }

    @PostMapping("/v1/videos/{videoId}/events", params = ["logVideoInteraction"])
    fun logVideoInteractedWithEvent(@PathVariable videoId: String, @RequestParam logVideoInteraction: Boolean, @RequestParam type: String?): ResponseEntity<Void> {
        saveVideoInteractedWithEvent.execute(videoId, type)
        return ResponseEntity(HttpStatus.OK)
    }
}
