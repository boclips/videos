package com.boclips.videos.service.application

import com.boclips.videos.service.infrastructure.event.EventService
import com.boclips.videos.service.presentation.PlaybackEvent
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock

class CreateEventTest {
    val playbackEvent = PlaybackEvent(
            playerIdentifier = "event-id",
            videoIdentifier = "v678",
            segmentStartSeconds = 10,
            segmentEndSeconds = 20,
            videoDurationSeconds = 60,
            captureTime = "2018-01-01T00:00:00.000Z"
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
    fun `validates player identifier`() {
        assertThatThrownBy { createEvent.execute(playbackEvent.copy(playerIdentifier = null)) }
        assertThatThrownBy { createEvent.execute(playbackEvent.copy(playerIdentifier = "")) }
    }

    @Test
    fun `validates video identifier`() {
        assertThatThrownBy { createEvent.execute(playbackEvent.copy(videoIdentifier = null)) }
        assertThatThrownBy { createEvent.execute(playbackEvent.copy(videoIdentifier = "")) }
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