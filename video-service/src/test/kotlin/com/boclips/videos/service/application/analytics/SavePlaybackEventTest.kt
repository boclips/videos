package com.boclips.videos.service.application.analytics

import com.boclips.eventbus.events.video.VideoSegmentPlayed
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

        val event = fakeEventBus.getEventOfType(VideoSegmentPlayed::class.java)

        assertThat(event.segmentEndSeconds).isEqualTo(20L)
        assertThat(event.segmentStartSeconds).isEqualTo(10L)
        assertThat(event.videoDurationSeconds).isEqualTo(60L)
    }
}
