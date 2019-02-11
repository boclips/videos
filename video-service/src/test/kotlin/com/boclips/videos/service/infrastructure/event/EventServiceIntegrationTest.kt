package com.boclips.videos.service.infrastructure.event

import com.boclips.videos.service.infrastructure.event.types.PlaybackEvent
import com.boclips.videos.service.infrastructure.event.types.SearchEvent
import com.boclips.videos.service.infrastructure.event.types.User
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.ZonedDateTime

class EventServiceIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var eventLogRepository: EventLogRepository

    @Test
    fun `searches are stored as analytics events`() {
        saveSearchEvent(ZonedDateTime.now())

        assertThat(eventLogRepository.count()).isEqualTo(1)
    }

    @Test
    fun `searches include user insights`() {
        saveSearchEvent(ZonedDateTime.now())

        assertThat(eventLogRepository.count()).isEqualTo(1)
    }

    @Test
    fun `status healthy when there are events`() {
        val playbackStandaloneTime = ZonedDateTime.now()
        val playbackSearchTime = playbackStandaloneTime.minusMinutes(1)
        val searchTime = playbackStandaloneTime.minusMinutes(2)

        saveSearchEvent(searchTime)
        savePlaybackEvent(playbackSearchTime, searchId = "search-id")
        savePlaybackEvent(playbackStandaloneTime, searchId = null)

        val status = eventService.status()

        assertThat(status.healthy).isTrue()
        assertThat(status.latestSearch).isEqualTo(searchTime)
        assertThat(status.latestPlaybackInSearch).isEqualTo(playbackSearchTime)
        assertThat(status.latestPlaybackStandalone).isEqualTo(playbackStandaloneTime)
    }

    @Test
    fun `status unhealthy when there are no playback events in the lookback period`() {
        val searchTime = ZonedDateTime.now()
        val playbackSearchTime = ZonedDateTime.now().minusHours(2)

        saveSearchEvent(searchTime)
        savePlaybackEvent(playbackSearchTime, searchId = "search-id")

        val status = eventService.status()

        assertThat(status.healthy).isFalse()
        assertThat(status.latestSearch).isEqualTo(searchTime)
        assertThat(status.latestPlaybackInSearch).isEqualTo(playbackSearchTime)
    }

    @Test
    fun `status unhealthy when there are no search events in the lookback period`() {
        val searchTime = ZonedDateTime.now().minusHours(2)
        val playbackSearchTime = ZonedDateTime.now()

        saveSearchEvent(searchTime)
        savePlaybackEvent(playbackSearchTime, searchId = "search-id")

        val status = eventService.status()

        assertThat(status.healthy).isFalse()
        assertThat(status.latestSearch).isEqualTo(searchTime)
        assertThat(status.latestPlaybackInSearch).isEqualTo(playbackSearchTime)
    }

    @Test
    fun `status unhealthy and null timestamps when no events`() {
        val status = eventService.status()

        assertThat(status.healthy).isFalse()
        assertThat(status.latestSearch).isNull()
        assertThat(status.latestPlaybackInSearch).isNull()
        assertThat(status.latestPlaybackStandalone).isNull()
    }

    @Test
    fun `returns no search results events`() {
        saveNoSearchResultsEvent()
        savePlaybackEvent(ZonedDateTime.now(), "e01")

        val events = eventService.getNoSearchResultsEvents()

        assertThat(events).hasSize(1)
    }

    private fun savePlaybackEvent(timestamp: ZonedDateTime, searchId: String?) {
        eventService.saveEvent(
            PlaybackEvent(
                playerId = "player-id",
                captureTime = timestamp,
                searchId = searchId,
                segmentStartSeconds = 10,
                segmentEndSeconds = 20,
                videoDurationSeconds = 50,
                videoId = "video-id",
                user = User.anonymous()
            )
        )
    }

    private fun saveNoSearchResultsEvent() {
        eventService.saveEvent(TestFactories.createNoSearchResultsEvent())
    }

    private fun saveSearchEvent(timestamp: ZonedDateTime) {
        eventService.saveEvent(SearchEvent(timestamp, "e01", User.anonymous(), "brownie", 9))
    }
}
