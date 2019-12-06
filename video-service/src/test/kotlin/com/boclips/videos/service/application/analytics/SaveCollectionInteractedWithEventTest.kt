package com.boclips.videos.service.application.analytics

import com.boclips.eventbus.events.collection.CollectionInteractedWith
import com.boclips.eventbus.events.collection.CollectionInteractionType
import com.boclips.videos.service.presentation.event.CollectionInteractedWithEventCommand
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.UserFactory
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class SaveCollectionInteractedWithEventTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var saveCollectionInteractedWithEvent: SaveCollectionInteractedWithEvent

    @Test
    fun `saves the event`() {
        saveCollectionInteractedWithEvent.execute(
            collectionId = "123", eventCommand = CollectionInteractedWithEventCommand(
                subtype = "NAVIGATE_TO_COLLECTION_DETAILS"
            ),
            user = UserFactory.sample(id = "123")
        )

        val event = fakeEventBus.getEventOfType(CollectionInteractedWith::class.java)

        Assertions.assertThat(event.subtype).isEqualTo(CollectionInteractionType.NAVIGATE_TO_COLLECTION_DETAILS)
        Assertions.assertThat(event.collectionId).isEqualTo("123")
    }
}
