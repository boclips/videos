package com.boclips.videos.service.infrastructure.search.event

import com.boclips.videos.service.infrastructure.event.Event
import com.boclips.videos.service.infrastructure.event.EventService
import com.boclips.videos.service.infrastructure.event.SearchEventData
import com.boclips.videos.service.presentation.video.SearchResource
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.hateoas.Resource
import java.time.ZonedDateTime

class SearchLoggerTest {

    @Test
    fun `adds searchId to the response`() {
        val eventService = mock<EventService>()

        val searchLogger = SearchLogger(eventService)

        val finalResource = searchLogger.logSearch(Resource(SearchResource(
                query = "some query",
                searchId = "",
                videos = listOf()))
        )

        assertThat(finalResource.content.searchId).isNotBlank()
    }

    @Test
    fun `logs search request to event service`() {
        val eventService = mock<EventService>()

        val searchLogger = SearchLogger(eventService)

        val searchId = searchLogger.logSearch(Resource(SearchResource(
                query = "some query",
                searchId = "",
                videos = listOf()))
        ).content.searchId

        verify(eventService).saveEvent<SearchEventData>(com.nhaarman.mockito_kotlin.check { event ->
            assertThat(event.timestamp).isAfter(ZonedDateTime.now().minusMinutes(1))
            assertThat(event.data.query).isEqualTo("some query")
            assertThat(event.data.searchId).isEqualTo(searchId)
            assertThat(event.data.resultsReturned).isEqualTo(0)
        })
    }
}