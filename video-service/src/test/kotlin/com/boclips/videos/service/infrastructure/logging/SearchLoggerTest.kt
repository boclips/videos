package com.boclips.videos.service.infrastructure.logging

import com.boclips.videos.service.infrastructure.event.EventService
import com.boclips.videos.service.infrastructure.event.types.SearchEventData
import com.boclips.videos.service.infrastructure.event.types.User
import com.boclips.videos.service.presentation.video.VideoResource
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.hateoas.Resource
import org.springframework.hateoas.Resources
import org.springframework.mock.web.MockHttpServletRequest
import java.time.ZonedDateTime

class SearchLoggerTest {

    private val emptyVideoList = Resources(emptyList<Resource<VideoResource>>())

    lateinit var eventService: EventService
    lateinit var searchLogger: SearchLogger

    @BeforeEach
    fun setUp() {
        eventService = mock()
        searchLogger = SearchLogger(eventService)
    }

    @Test
    fun `logs search request to event service when correlation id header is present`() {
        val currentRequest = MockHttpServletRequest()

        searchLogger.logSearch(emptyVideoList, currentRequest, mockPrincipal(), "some query")

        verify(eventService).saveEvent<SearchEventData>(com.nhaarman.mockito_kotlin.check { event ->
            assertThat(event.timestamp).isAfter(ZonedDateTime.now().minusMinutes(1))
            assertThat(event.data.query).isEqualTo("some query")
            assertThat(event.data.resultsReturned).isEqualTo(0)
        })
    }

    @Test
    fun `builds event with principal details`() {
        val currentRequest = MockHttpServletRequest()

        searchLogger.logSearch(emptyVideoList, currentRequest, User(true, "myid"), "some query")

        verify(eventService).saveEvent<SearchEventData>(com.nhaarman.mockito_kotlin.check { event ->
            assertThat(event.user.boclipsEmployee).isTrue()
            assertThat(event.user.id).isEqualTo("myid")
        })
    }

    fun mockPrincipal() = User.anonymous()
}