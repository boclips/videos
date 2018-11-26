package com.boclips.videos.service.application.event

import com.boclips.videos.service.infrastructure.email.EmailClient
import com.boclips.videos.service.infrastructure.event.EventService
import com.boclips.videos.service.infrastructure.event.types.NoSearchResultsEvent
import com.boclips.videos.service.infrastructure.event.types.PlaybackEvent
import com.boclips.videos.service.infrastructure.event.types.SearchEventData
import com.boclips.videos.service.presentation.event.CreateNoSearchResultsEventCommand
import com.boclips.videos.service.presentation.event.CreatePlaybackEventCommand
import com.boclips.videos.service.testsupport.setSecurityContext
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.verify
import com.sun.security.auth.UserPrincipal
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.test.context.TestSecurityContextHolder

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
    lateinit var eventService: EventService

    @BeforeEach
    fun setUp() {
        eventService = mock(EventService::class.java)
        emailClient = mock(EmailClient::class.java)
        createEvent = CreateEvent(eventService, emailClient)
    }

    @Test
    fun `extracts user data for playback event`() {
        setSecurityContext(UserPrincipal("testing@boclips.com"))

        createEvent.execute(playbackEvent)

        verify(eventService).saveEvent(com.nhaarman.mockito_kotlin.check { event: PlaybackEvent ->
            Assertions.assertThat(event.user.boclipsEmployee).isTrue()
        })
    }

    @Test
    fun `validates a valid playback event`() {
        assertThatCode { createEvent.execute(playbackEvent) }.doesNotThrowAnyException()
    }

    @Test
    fun `validates a valid no results event`() {
        assertThatCode { createEvent.execute(noResultsEvent) }.doesNotThrowAnyException()
    }

    @Test
    fun `handles well formed capture time`() {
        assertThatCode { createEvent.execute(playbackEvent) }.doesNotThrowAnyException()
    }

    @Test
    fun `handles badly formed capture time`() {
        assertThatCode { createEvent.execute(playbackEvent.copy(captureTime = "abc")) }.doesNotThrowAnyException()
    }

    @Test
    fun `extracts user data for no results event`() {
        SecurityContextHolder.setContext(SecurityContextImpl(TestingAuthenticationToken(UserPrincipal("teacher@boclips.com"), null)))

        createEvent.execute(noResultsEvent)

        verify(eventService).saveEvent(com.nhaarman.mockito_kotlin.check { event: NoSearchResultsEvent ->
            Assertions.assertThat(event.user.boclipsEmployee).isTrue()
        })
    }

    @Test
    fun `sends email to log no search results event`() {
        createEvent.execute(noResultsEvent)

        verify(emailClient).send(any())
    }
}
