package com.boclips.videos.service.application.video

import com.boclips.events.types.VideoPlaybackSyncRequested
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class RequestVideoPlaybackUpdateTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var requestVideoPlaybackUpdate: RequestVideoPlaybackUpdate

    @Test
    fun `publishes one event per video to be updated`() {
        val asset = saveVideo()
        saveVideo()

        requestVideoPlaybackUpdate.invoke()

        val message = messageCollector.forChannel(topics.videoPlaybackSyncRequested()).poll()
        val event = objectMapper.readValue(message.payload.toString(), VideoPlaybackSyncRequested::class.java)
        Assertions.assertThat(event.videoId).isEqualTo(asset.value)
    }
}