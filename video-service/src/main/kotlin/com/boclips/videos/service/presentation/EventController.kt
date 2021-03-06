package com.boclips.videos.service.presentation

import com.boclips.videos.service.application.analytics.SaveCollectionInteractedWithEvent
import com.boclips.videos.service.application.analytics.SavePlaybackEvent
import com.boclips.videos.service.application.analytics.SavePlayerInteractedWithEvent
import com.boclips.videos.service.application.analytics.SaveSearchQuerySuggestionsCompletedEvent
import com.boclips.videos.service.application.analytics.SaveVideoInteractedWithEvent
import com.boclips.videos.service.domain.service.GetUserIdOverride
import com.boclips.videos.service.domain.service.user.AccessRuleService
import com.boclips.videos.service.domain.service.user.UserService
import com.boclips.videos.service.presentation.event.CollectionInteractedWithEventCommand
import com.boclips.videos.service.presentation.event.CreatePlaybackEventCommand
import com.boclips.videos.service.presentation.event.CreatePlayerInteractedWithEvent
import com.boclips.videos.service.presentation.event.SearchQuerySuggestionsCompletedEvent
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
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
    private val saveCollectionInteractedWithEvent: SaveCollectionInteractedWithEvent,
    private val saveSearchQuerySuggestionsCompletedEvent: SaveSearchQuerySuggestionsCompletedEvent,
    getUserIdOverride: GetUserIdOverride,
    accessRuleService: AccessRuleService,
    userService: UserService
) : BaseController(accessRuleService, getUserIdOverride, userService) {

    @PostMapping("/v1/events/playback")
    fun logPlaybackEvent(
        @RequestBody playbackEvent: CreatePlaybackEventCommand?
    ): ResponseEntity<Void> {
        savePlaybackEvent.execute(playbackEvent, getCurrentUser())
        return ResponseEntity(HttpStatus.CREATED)
    }

    @PostMapping("/v1/events/playback/batch")
    fun batchLogPlaybackEvent(@RequestBody playbackEvent: List<CreatePlaybackEventCommand>?): ResponseEntity<Void> {
        savePlaybackEvent.execute(playbackEvent, getCurrentUser())
        return ResponseEntity(HttpStatus.CREATED)
    }

    @PostMapping("/v1/events/player-interaction")
    fun logPlayerInteractedWithEvent(@RequestBody playbackEvent: CreatePlayerInteractedWithEvent?): ResponseEntity<Void> {
        savePlayerInteractedWithEvent.execute(playbackEvent, getCurrentUser())
        return ResponseEntity(HttpStatus.CREATED)
    }

    @PostMapping("v1/collections/{collectionId}/events")
    fun logCollectionInteractedWithEvent(
        @PathVariable collectionId: String,
        @RequestBody data: CollectionInteractedWithEventCommand?
    ): ResponseEntity<Void> {
        saveCollectionInteractedWithEvent.execute(collectionId, data, getCurrentUser())
        return ResponseEntity(HttpStatus.OK)
    }

    @PostMapping("/v1/videos/{videoId}/events", params = ["logVideoInteraction"])
    fun logVideoInteractedWithEvent(
        @PathVariable videoId: String,
        @RequestParam logVideoInteraction: Boolean,
        @RequestParam type: String?
    ): ResponseEntity<Void> {
        saveVideoInteractedWithEvent.execute(videoId, type, getCurrentUser())
        return ResponseEntity(HttpStatus.OK)
    }

    @PostMapping("/v1/events/suggested-search-completions")
    fun logSearchQuerySuggestionsCompletedEvent(@RequestBody event: SearchQuerySuggestionsCompletedEvent?): ResponseEntity<Void> {
        saveSearchQuerySuggestionsCompletedEvent.execute(event, getCurrentUser())
        return ResponseEntity(HttpStatus.CREATED)
    }
}
