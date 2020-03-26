package com.boclips.videos.service.presentation

import com.boclips.videos.service.application.analytics.SaveCollectionInteractedWithEvent
import com.boclips.videos.service.application.analytics.SavePlaybackEvent
import com.boclips.videos.service.application.analytics.SavePlayerInteractedWithEvent
import com.boclips.videos.service.application.analytics.SaveVideoInteractedWithEvent
import com.boclips.videos.service.domain.service.user.AccessRuleService
import com.boclips.videos.service.domain.service.GetUserIdOverride
import com.boclips.videos.service.presentation.event.CollectionInteractedWithEventCommand
import com.boclips.videos.service.presentation.event.CreatePlaybackEventCommand
import com.boclips.videos.service.presentation.event.CreatePlayerInteractedWithEvent
import com.boclips.videos.service.presentation.support.Cookies
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
    private val saveCollectionInteractedWithEvent: SaveCollectionInteractedWithEvent,
    getUserIdOverride: GetUserIdOverride,
    accessRuleService: AccessRuleService
) : BaseController(accessRuleService, getUserIdOverride) {

    @PostMapping("/v1/events/playback")
    fun logPlaybackEvent(@RequestBody playbackEvent: CreatePlaybackEventCommand?, @CookieValue(Cookies.PLAYBACK_DEVICE) playbackDevice: String? = null): ResponseEntity<Void> {
        savePlaybackEvent.execute(playbackEvent, playbackDevice, getCurrentUser())
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
    fun logCollectionInteractedWithEvent(@PathVariable collectionId: String, @RequestBody data: CollectionInteractedWithEventCommand?): ResponseEntity<Void> {
        saveCollectionInteractedWithEvent.execute(collectionId, data, getCurrentUser())
        return ResponseEntity(HttpStatus.OK)
    }

    @PostMapping("/v1/videos/{videoId}/events", params = ["logVideoInteraction"])
    fun logVideoInteractedWithEvent(@PathVariable videoId: String, @RequestParam logVideoInteraction: Boolean, @RequestParam type: String?): ResponseEntity<Void> {
        saveVideoInteractedWithEvent.execute(videoId, type, getCurrentUser())
        return ResponseEntity(HttpStatus.OK)
    }
}
