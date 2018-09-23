package com.boclips.videos.service.application

import com.boclips.videos.service.application.event.CreateEvent
import com.boclips.videos.service.infrastructure.event.EventService
import com.boclips.videos.service.application.event.CreatePlaybackEventCommand
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock

class CreateEventTest {
    val playbackEvent = CreatePlaybackEventCommand(
            playerId = "player-id",
            videoId = "v678",
            segmentStartSeconds = 10,
            segmentEndSeconds = 20,
            videoDurationSeconds = 60,
            captureTime = "2018-01-01T00:00:00.000Z",
            searchId = "search-id"
    )


    lateinit var createEvent: CreateEvent

    @Before
    fun setUp() {
        val eventService = mock(EventService::class.java)
        createEvent = CreateEvent(eventService)
    }

    @Test
    fun `validates a valid playback event`() {
        assertThatCode { createEvent.execute(playbackEvent) }.doesNotThrowAnyException()
    }

    @Test
    fun `handles null object`() {
        assertThatThrownBy { createEvent.execute(null) }
    }

    @Test
    fun `validates player identifier`() {
        assertThatThrownBy { createEvent.execute(playbackEvent.copy(playerId = null)) }
        assertThatThrownBy { createEvent.execute(playbackEvent.copy(playerId = "")) }
    }

    @Test
    fun `validates video identifier`() {
        assertThatThrownBy { createEvent.execute(playbackEvent.copy(videoId = null)) }
        assertThatThrownBy { createEvent.execute(playbackEvent.copy(videoId = "")) }
    }

    @Test
    fun `validates segmentStartSeconds`() {
        assertThatThrownBy { createEvent.execute(playbackEvent.copy(segmentStartSeconds = null)) }
        assertThatThrownBy { createEvent.execute(playbackEvent.copy(segmentStartSeconds = -1)) }
    }

    @Test
    fun `validates segmentEndSeconds`() {
        assertThatThrownBy { createEvent.execute(playbackEvent.copy(segmentEndSeconds = null)) }
        assertThatThrownBy { createEvent.execute(playbackEvent.copy(segmentEndSeconds = -1)) }
    }

    @Test
    fun `validates videoDurationSeconds`() {
        assertThatThrownBy { createEvent.execute(playbackEvent.copy(videoDurationSeconds = null)) }
        assertThatThrownBy { createEvent.execute(playbackEvent.copy(videoDurationSeconds = -1)) }
    }

    @Test
    fun `validates captureTime`() {
        assertThatThrownBy { createEvent.execute(playbackEvent.copy(captureTime = null)) }
        assertThatThrownBy { createEvent.execute(playbackEvent.copy(captureTime = "not a valid date-time")) }
    }
}
