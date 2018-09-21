package com.boclips.videos.service.infrastructure.event

import com.boclips.videos.service.application.PlaybackEvent
import com.boclips.videos.service.infrastructure.search.SearchEvent
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.ZonedDateTime

class EventServiceIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var eventService: EventService

    @Autowired
    lateinit var eventLogRepository: EventLogRepository

    @Test
    fun `searches are stored as analytics events`() {
        saveSearchEvent(ZonedDateTime.now())

        assertThat(eventLogRepository.count()).isEqualTo(1)
    }

    @Test
    fun `status healthy when there are events`() {
        saveSearchEvent(ZonedDateTime.now())
        savePlaybackEvent(ZonedDateTime.now())

        assertThat(eventService.status()).isTrue()
    }

    @Test
    fun `status unhealthy when there are no playback events in the lookback period`() {
        saveSearchEvent(ZonedDateTime.now())
        savePlaybackEvent(ZonedDateTime.now().minusHours(2))

        assertThat(eventService.status()).isFalse()
    }

    @Test
    fun `status unhealthy when there are no search events in the lookback period`() {
        saveSearchEvent(ZonedDateTime.now().minusHours(2))
        savePlaybackEvent(ZonedDateTime.now())

        assertThat(eventService.status()).isFalse()
    }

    private fun savePlaybackEvent(timestamp: ZonedDateTime) {
        eventLogRepository.save(PlaybackEvent(
                playerIdentifier = "player-id",
                captureTime = timestamp,
                searchId = "search-id",
                segmentStartSeconds = 10,
                segmentEndSeconds = 20,
                videoDurationSeconds = 50,
                videoIdentifier = "video-id"
        ))
    }

    private fun saveSearchEvent(timestamp: ZonedDateTime) {
        eventService.saveEvent(SearchEvent(timestamp, "e01", "brownie", 9))
    }
}
