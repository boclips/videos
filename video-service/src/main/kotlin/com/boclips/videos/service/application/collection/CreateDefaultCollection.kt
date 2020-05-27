package com.boclips.videos.service.application.collection

import com.boclips.eventbus.BoclipsEventListener
import com.boclips.eventbus.events.user.UserCreated
import com.boclips.videos.service.domain.model.collection.CreateDefaultCollectionCommand
import com.boclips.videos.service.domain.model.user.UserId
import com.boclips.videos.service.domain.service.collection.CollectionCreationService
import mu.KLogging

class CreateDefaultCollection(private val service: CollectionCreationService) {
    companion object : KLogging()

    @BoclipsEventListener
    fun onUserCreated(event: UserCreated) {
        val owner = UserId(event.user.id)
        val command = CreateDefaultCollectionCommand(owner)
        service.create(command)
        logger.info { "Created default collection for user $owner" }
    }
}
