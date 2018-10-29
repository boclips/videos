package com.boclips.videos.service.application

import com.boclips.videos.service.application.event.CreateEvent
import com.boclips.videos.service.infrastructure.event.EventService
import com.boclips.videos.service.presentation.event.CreateNoSearchResultsEventCommand
import com.boclips.videos.service.presentation.event.CreatePlaybackEventCommand
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

    val noResultsEvent = CreateNoSearchResultsEventCommand(
            name = "Hans",
            email = "hi@there.com",
            description = "none",
            query = "animal"
    )

    lateinit var createEvent: CreateEvent

    @Before
    fun setUp() {
        val eventService = mock(EventService::class.java)
        createEvent = CreateEvent(eventService)
    }

    @Test
    fun `validates a valid playback event`() {
        assertThatCode { createEvent.createPlaybackEvent(playbackEvent) }.doesNotThrowAnyException()
    }

    @Test
    fun `validates a valid no results event`() {
        assertThatCode { createEvent.createNoSearchResultsEvent(noResultsEvent) }.doesNotThrowAnyException()
    }

    @Test
    fun `handles well formed capture time`() {
        assertThatCode { createEvent.createPlaybackEvent(playbackEvent) }.doesNotThrowAnyException()
    }

    @Test
    fun `handles badly formed capture time`() {
        assertThatCode { createEvent.createPlaybackEvent(playbackEvent.copy(captureTime = "abc")) }.doesNotThrowAnyException()
    }

    @Test
    fun `handles null object`() {
        assertThatThrownBy { createEvent.createPlaybackEvent(null) }
    }
}
