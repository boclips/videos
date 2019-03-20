package com.boclips.videos.service.application.analytics

import com.boclips.videos.service.presentation.event.CreatePlaybackEventCommand
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.fakes.FakeAnalyticsEventService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SavePlaybackEventTest {

    private val payload = CreatePlaybackEventCommand(
        playerId = "player-id",
        videoId = TestFactories.aValidId(),
        videoIndex = null,
        segmentStartSeconds = 10,
        segmentEndSeconds = 20,
        videoDurationSeconds = 60
    )

    lateinit var savePlaybackEvent: SavePlaybackEvent
    lateinit var eventService: FakeAnalyticsEventService

    @BeforeEach
    fun setUp() {
        eventService = FakeAnalyticsEventService()
        savePlaybackEvent = SavePlaybackEvent(eventService)
    }

    @Test
    fun `saves the event`() {
        savePlaybackEvent.execute(payload)

        val event = eventService.playbackEvent()

        assertThat(event.data.segmentStartSeconds).isEqualTo(10)
    }
}
