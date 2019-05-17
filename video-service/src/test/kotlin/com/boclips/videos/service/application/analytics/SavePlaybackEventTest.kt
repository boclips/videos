package com.boclips.videos.service.application.analytics

import com.boclips.videos.service.presentation.event.CreatePlaybackEventCommand
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class SavePlaybackEventTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var savePlaybackEvent: SavePlaybackEvent

    private val payload = CreatePlaybackEventCommand(
        playerId = "player-id",
        videoId = TestFactories.aValidId(),
        videoIndex = null,
        segmentStartSeconds = 10,
        segmentEndSeconds = 20,
        videoDurationSeconds = 60
    )

    @Test
    fun `saves the event`() {
        savePlaybackEvent.execute(payload)

        val event = messageCollector.forChannel(topics.videoSegmentPlayed()).poll()

        assertThat(event).isNotNull
        assertThat(event.payload.toString()).contains("player-id")
        assertThat(event.payload.toString()).contains("10")
        assertThat(event.payload.toString()).contains("20")
        assertThat(event.payload.toString()).contains("60")
    }
}
