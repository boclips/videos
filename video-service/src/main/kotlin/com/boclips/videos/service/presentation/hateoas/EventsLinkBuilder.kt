package com.boclips.videos.service.presentation.hateoas

import com.boclips.security.utils.UserExtractor
import com.boclips.videos.api.response.HateoasLink
import com.boclips.videos.service.config.security.UserRoles
import com.boclips.videos.service.presentation.EventController
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn
import org.springframework.stereotype.Component

@Component
class EventsLinkBuilder {
    fun createPlaybackEventLink(): HateoasLink {
        return HateoasLink.of(
            linkTo(
                methodOn(EventController::class.java).logPlaybackEvent(null)
            ).withRel("createPlaybackEvent")
        )
    }

    fun createPlaybackEventsLink(): HateoasLink? {
        return UserExtractor.getIfHasRole(UserRoles.INSERT_EVENTS) {
            HateoasLink.of(
                linkTo(
                    methodOn(EventController::class.java).batchLogPlaybackEvent(null)
                ).withRel("createPlaybackEvents")
            )
        }
    }

    fun createPlayerInteractedWithEventLink(): HateoasLink {
        return HateoasLink.of(
            linkTo(
                methodOn(EventController::class.java).logPlayerInteractedWithEvent(null)
            ).withRel("createPlayerInteractedWithEvent")
        )
    }
}
