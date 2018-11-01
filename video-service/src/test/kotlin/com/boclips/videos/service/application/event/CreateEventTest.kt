package com.boclips.videos.service.application.event

import com.boclips.videos.service.infrastructure.email.EmailClient
import com.boclips.videos.service.infrastructure.event.EventService
import com.boclips.videos.service.presentation.event.CreateNoSearchResultsEventCommand
import com.boclips.videos.service.presentation.event.CreatePlaybackEventCommand
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.verify
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
    lateinit var emailClient: EmailClient

    @Before
    fun setUp() {
        val eventService = mock(EventService::class.java)
        emailClient = mock(EmailClient::class.java)

        createEvent = CreateEvent(eventService, emailClient)
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

    @Test
    fun `sends email to log no search results event`() {
        createEvent.createNoSearchResultsEvent(noResultsEvent)

        verify(emailClient).send(any())
    }
}
