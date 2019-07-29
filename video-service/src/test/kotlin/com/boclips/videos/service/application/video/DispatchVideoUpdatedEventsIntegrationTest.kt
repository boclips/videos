package com.boclips.videos.service.application.video

import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class DispatchVideoUpdatedEventsIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var dispatchVideoUpdatedEvents: DispatchVideoUpdatedEvents

    @Test
    fun `dispatches an event for every video`() {
        saveVideo(title = "title 1")
        saveVideo(title = "title 2")

        dispatchVideoUpdatedEvents()

        assertThat(fakeEventBus.receivedEvents).hasSize(2)
    }
}
