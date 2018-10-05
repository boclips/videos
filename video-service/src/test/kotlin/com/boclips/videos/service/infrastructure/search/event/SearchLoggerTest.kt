package com.boclips.videos.service.infrastructure.search.event

import com.boclips.videos.service.infrastructure.event.EventService
import com.boclips.videos.service.infrastructure.event.SearchEventData
import com.boclips.videos.service.presentation.video.VideoResource
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.springframework.hateoas.Resource
import org.springframework.hateoas.Resources
import org.springframework.mock.web.MockHttpServletRequest
import java.time.ZonedDateTime

class SearchLoggerTest {

    private val emptyVideoList = Resources(emptyList<Resource<VideoResource>>())

    lateinit var eventService: EventService
    lateinit var searchLogger: SearchLogger

    @Before
    fun setUp() {
        eventService = mock()
        searchLogger = SearchLogger(eventService)
    }

    @Test
    fun `preserves the correlation id header`() {
        val currentRequest = MockHttpServletRequest()
        currentRequest.addHeader(SearchLogger.X_CORRELATION_ID, "correlation id")

        val finalResource = searchLogger.logSearch(emptyVideoList, currentRequest, "some query")

        assertThat(finalResource.headers["X-Correlation-ID"]).containsExactly("correlation id")
    }

    @Test
    fun `logs search request to event service when correlation id header is present`() {
        val currentRequest = MockHttpServletRequest()
        currentRequest.addHeader(SearchLogger.X_CORRELATION_ID, "correlation id")

        searchLogger.logSearch(emptyVideoList, currentRequest, "some query")

        verify(eventService).saveEvent<SearchEventData>(com.nhaarman.mockito_kotlin.check { event ->
            assertThat(event.timestamp).isAfter(ZonedDateTime.now().minusMinutes(1))
            assertThat(event.data.query).isEqualTo("some query")
            assertThat(event.data.searchId).isEqualTo("correlation id")
            assertThat(event.data.resultsReturned).isEqualTo(0)
        })
    }

    @Test
    fun `generates UUID when correlation id header is missing`() {
        val currentRequest = MockHttpServletRequest()

        searchLogger.logSearch(emptyVideoList, currentRequest, "some query")

        verify(eventService).saveEvent<SearchEventData>(com.nhaarman.mockito_kotlin.check { event ->
            assertThat(event.data.searchId).isNotBlank()
        })
    }
}