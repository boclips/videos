package com.boclips.videos.service.presentation.event

import com.boclips.eventbus.events.collection.CollectionInteractionType
import com.boclips.videos.service.application.analytics.InvalidEventException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CollectionInteractedWithEventCommandTest {

    @Test
    fun `can convert valid subtype`() {
        val command = CollectionInteractedWithEventCommand(subtype = "NAVIGATE_TO_COLLECTION_DETAILS")

        assertThat(command.getSubtype()).isEqualTo(CollectionInteractionType.NAVIGATE_TO_COLLECTION_DETAILS)
    }

    @Test
    fun `throws when trying to convert invalid subtype`() {
        val command = CollectionInteractedWithEventCommand(subtype = "sometjing")

        assertThrows<InvalidEventException> { command.getSubtype() }
    }
}
