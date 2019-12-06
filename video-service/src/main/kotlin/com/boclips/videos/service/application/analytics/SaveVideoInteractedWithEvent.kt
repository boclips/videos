package com.boclips.videos.service.application.analytics

import com.boclips.security.utils.User
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.events.EventService

class SaveVideoInteractedWithEvent(val eventService: EventService) {
    fun execute(videoId: String, type: String?, user: User) {
        type ?: throw InvalidEventException("Type cannot be null")

        eventService.publishVideoInteractedWithEvent(VideoId(videoId), type, user)
    }
}
