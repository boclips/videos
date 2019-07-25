package com.boclips.videos.service.application.video

import com.boclips.eventbus.events.video.VideoUpdated
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class DispatchVideoUpdatedEventsIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var dispatchVideoUpdatedEvents: DispatchVideoUpdatedEvents

    @Test
    fun `dispatches an event for every video`() {
        saveVideo(title = "waterfalls")

        dispatchVideoUpdatedEvents()

        val event = fakeEventBus.getEventOfType(VideoUpdated::class.java)
        assertThat(event.video.title).isEqualTo("waterfalls")
    }
}
