package com.boclips.videos.service.application.video

import com.boclips.eventbus.events.video.VideoPlaybackSyncRequested
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class RequestPlaybackUpdateIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var requestPlaybackUpdate: RequestPlaybackUpdate

    @Test
    fun `publishes one event per video to be updated`() {
        saveVideo()
        saveVideo()

        requestPlaybackUpdate()

        assertThat(fakeEventBus.countEventsOfType(VideoPlaybackSyncRequested::class.java)).isEqualTo(2)
    }
}
