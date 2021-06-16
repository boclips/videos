package com.boclips.videos.service.application.collection

import com.boclips.eventbus.events.collection.CollectionBroadcastRequested
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

internal class BroadcastCollectionsIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var broadcastCollections: BroadcastCollections

    @Test
    fun `dispatches an event for every collection`() {
        saveCollection(title = "title 1")
        saveCollection(title = "title 2")

        broadcastCollections()

        val events = fakeEventBus.getEventsOfType(CollectionBroadcastRequested::class.java)
        Assertions.assertThat(events).hasSize(2)
    }
}
