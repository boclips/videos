package com.boclips.videos.service.presentation.hateoas

import com.boclips.videos.service.presentation.EventController
import org.springframework.hateoas.mvc.ControllerLinkBuilder
import org.springframework.stereotype.Component

@Component
class EventsLinkBuilder {

    fun createPlaybackEventLink() = ControllerLinkBuilder.linkTo(
        ControllerLinkBuilder.methodOn(EventController::class.java).logPlaybackEvent(null)
    ).withRel("createPlaybackEvent")

    fun createPlayerInteractedWithEventLink() = ControllerLinkBuilder.linkTo(
        ControllerLinkBuilder.methodOn(EventController::class.java).logPlayerInteractedWithEvent(null)
    ).withRel("createPlayerInteractedWithEvent")

    fun createNoResultsEventLink() = ControllerLinkBuilder.linkTo(
        ControllerLinkBuilder.methodOn(EventController::class.java).logNoSearchResultsEvent(null)
    ).withRel("createNoSearchResultsEvent")

    fun createVideoVisitedEventLink() = ControllerLinkBuilder.linkTo(
        ControllerLinkBuilder.methodOn(EventController::class.java).logVideoVisitedEvent(null)
    ).withRel("createVideoVisitedEvent")

}
