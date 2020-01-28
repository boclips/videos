package com.boclips.videos.service.presentation.hateoas

import com.boclips.security.utils.UserExtractor
import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.presentation.EventController
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder
import org.springframework.stereotype.Component

@Component
class EventsLinkBuilder {
    fun createPlaybackEventLink() = WebMvcLinkBuilder.linkTo(
        WebMvcLinkBuilder.methodOn(EventController::class.java).logPlaybackEvent(null, null)
    ).withRel("createPlaybackEvent")

    fun createPlaybackEventsLink() = UserExtractor.getIfHasRole(UserRoles.INSERT_EVENTS) {
        WebMvcLinkBuilder.linkTo(
            WebMvcLinkBuilder.methodOn(EventController::class.java).batchLogPlaybackEvent(null)
        ).withRel("createPlaybackEvents")
    }

    fun createPlayerInteractedWithEventLink() = WebMvcLinkBuilder.linkTo(
        WebMvcLinkBuilder.methodOn(EventController::class.java).logPlayerInteractedWithEvent(null)
    ).withRel("createPlayerInteractedWithEvent")
}
