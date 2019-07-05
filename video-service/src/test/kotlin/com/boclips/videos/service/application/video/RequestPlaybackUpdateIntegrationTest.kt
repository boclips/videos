package com.boclips.videos.service.application.video

import com.boclips.events.types.video.VideoPlaybackSyncRequested
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class RequestPlaybackUpdateIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var requestPlaybackUpdate: RequestPlaybackUpdate

    @BeforeEach
    fun setup() {
        messageCollector.forChannel(topics.videoPlaybackSyncRequested()).clear()
    }

    @Test
    fun `publishes one event per video to be updated`() {
        val video = saveVideo()
        saveVideo()

        requestPlaybackUpdate()

        val message = messageCollector.forChannel(topics.videoPlaybackSyncRequested()).poll()
        val event = objectMapper.readValue(message.payload.toString(), VideoPlaybackSyncRequested::class.java)
        assertThat(event.videoId).isEqualTo(video.value)
    }
}
