package com.boclips.videos.service.presentation.hateoas

import com.boclips.videos.service.presentation.EventController
import org.springframework.hateoas.mvc.ControllerLinkBuilder
import org.springframework.stereotype.Component

@Component
class EventsLinkBuilder {

    fun createPlaybackEventLink() = ControllerLinkBuilder.linkTo(
        ControllerLinkBuilder.methodOn(EventController::class.java).logPlaybackEvent(null)
    ).withRel("createPlaybackEvent")

    fun createNoResultsEventLink() = ControllerLinkBuilder.linkTo(
        ControllerLinkBuilder.methodOn(EventController::class.java).logNoSearchResultsEvent(null)
    ).withRel("createNoSearchResultsEvent")
}
