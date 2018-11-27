package com.boclips.videos.service.infrastructure.event

import com.boclips.videos.service.infrastructure.event.types.EventEntity
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime


class EventEntityTest {

    @Test
    fun `timestamp is normalized to UTC`() {
        val timestamp = ZonedDateTime.of(2018, 1, 1, 10, 0, 0, 0, ZoneOffset.ofHours(10))

        val event = TestFactories.createSearchEvent(timestamp = timestamp)

        assertThat(EventEntity.fromEvent(event).timestamp).isEqualTo(LocalDateTime.of(2018, 1, 1, 0, 0, 0))
    }

    @Test
    fun `SearchEvents can be round-tripped through Mongo`() {
        val searchEvent = TestFactories.createSearchEvent()

        val entity = EventEntity.fromEvent(searchEvent)

        assertThat(entity.toEvent()).isEqualToComparingFieldByField(searchEvent)
    }

    @Test
    fun `PlaybackEvents can be round-tripped through Mongo`() {
        val playbackEvent = TestFactories.createPlaybackEvent()

        val entity = EventEntity.fromEvent(playbackEvent)

        assertThat(entity.toEvent()).isEqualToComparingFieldByField(playbackEvent)
    }

    @Test
    fun `NoSearchResultsEvents can be round-tripped through Mongo`() {
        val noSearchResultsEvent = TestFactories.createNoSearchResultsEvent()

        val entity = EventEntity.fromEvent(noSearchResultsEvent)

        assertThat(entity.toEvent()).isEqualToComparingFieldByField(noSearchResultsEvent)
    }

}
