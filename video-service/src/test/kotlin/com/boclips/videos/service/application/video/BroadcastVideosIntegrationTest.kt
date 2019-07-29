package com.boclips.videos.service.application.video

import com.boclips.eventbus.events.video.VideoBroadcastRequested
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class BroadcastVideosIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var broadcastVideos: BroadcastVideos

    @Test
    fun `dispatches an event for every video`() {
        saveVideo(title = "title 1")
        saveVideo(title = "title 2")

        broadcastVideos()

        val events = fakeEventBus.getEventsOfType(VideoBroadcastRequested::class.java)
        assertThat(events).hasSize(2)
    }
}
