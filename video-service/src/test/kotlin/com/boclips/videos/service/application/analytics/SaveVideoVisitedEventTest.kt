package com.boclips.videos.service.application.analytics

import com.boclips.videos.service.presentation.event.CreateVideoVisitedEventCommand
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired

class SaveVideoVisitedEventTest: AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var saveVideoVisitedEvent: SaveVideoVisitedEvent

    @Test
    fun `throws when command is null`() {
        assertThrows<InvalidEventException> {
            saveVideoVisitedEvent.execute(null)
        }
    }

    @Test
    fun `throws when command is not valid`() {
        assertThrows<InvalidEventException> {
            saveVideoVisitedEvent.execute(CreateVideoVisitedEventCommand(videoId = null))
        }
    }
}

