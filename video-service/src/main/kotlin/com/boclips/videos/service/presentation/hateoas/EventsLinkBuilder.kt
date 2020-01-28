package com.boclips.videos.service.presentation.hateoas

import com.boclips.security.utils.UserExtractor
import com.boclips.videos.api.response.HateoasLink
import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.presentation.EventController
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder
import org.springframework.stereotype.Component

@Component
class EventsLinkBuilder {
    fun createPlaybackEventLink(): HateoasLink {
        return HateoasLink.of(
            WebMvcLinkBuilder.linkTo(
                WebMvcLinkBuilder.methodOn(EventController::class.java).logPlaybackEvent(null, null)
            ).withRel("createPlaybackEvent")
        )
    }

    fun createPlaybackEventsLink(): HateoasLink? {
        return UserExtractor.getIfHasRole(UserRoles.INSERT_EVENTS) {
            HateoasLink.of(
                WebMvcLinkBuilder.linkTo(
                    WebMvcLinkBuilder.methodOn(EventController::class.java).batchLogPlaybackEvent(null)
                ).withRel("createPlaybackEvents")
            )
        }
    }

    fun createPlayerInteractedWithEventLink(): HateoasLink {
        return HateoasLink.of(
            WebMvcLinkBuilder.linkTo(
                WebMvcLinkBuilder.methodOn(EventController::class.java).logPlayerInteractedWithEvent(null)
            ).withRel("createPlayerInteractedWithEvent")
        )
    }
}
