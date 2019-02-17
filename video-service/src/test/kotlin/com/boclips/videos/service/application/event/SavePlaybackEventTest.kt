package com.boclips.videos.service.application.event

import com.boclips.videos.service.presentation.event.CreatePlaybackEventCommand
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.fakes.FakeEventService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SavePlaybackEventTest {

    private val payload = CreatePlaybackEventCommand(
            playerId = "player-id",
            assetId = TestFactories.aValidId(),
            videoIndex = null,
            segmentStartSeconds = 10,
            segmentEndSeconds = 20,
            videoDurationSeconds = 60
    )

    lateinit var savePlaybackEvent: SavePlaybackEvent
    lateinit var eventService: FakeEventService

    @BeforeEach
    fun setUp() {
        eventService = FakeEventService()
        savePlaybackEvent = SavePlaybackEvent(eventService)
    }

    @Test
    fun `saves the event`() {
        savePlaybackEvent.execute(payload)

        val event = eventService.playbackEvent()

        assertThat(event.data.segmentStartSeconds).isEqualTo(10)
    }
}
