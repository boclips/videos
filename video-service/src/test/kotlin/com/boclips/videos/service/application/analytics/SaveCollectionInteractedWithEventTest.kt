package com.boclips.videos.service.application.analytics

import com.boclips.eventbus.events.collection.CollectionInteractedWith
import com.boclips.videos.service.presentation.event.CreateCollectionInteractedWithEvent
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class SaveCollectionInteractedWithEventTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var saveCollectionInteractedWithEvent: SaveCollectionInteractedWithEvent

    @Test
    fun `saves the event`() {
        saveCollectionInteractedWithEvent.execute(collectionId = "123", event = CreateCollectionInteractedWithEvent(
            subtype = "NAVIGATE_TO_COLLECTION_DETAILS"
        ))

        val event = fakeEventBus.getEventOfType(CollectionInteractedWith::class.java)

        Assertions.assertThat(event.subtype).isEqualTo("NAVIGATE_TO_COLLECTION_DETAILS")
        Assertions.assertThat(event.collectionId).isEqualTo("123")
    }
}
