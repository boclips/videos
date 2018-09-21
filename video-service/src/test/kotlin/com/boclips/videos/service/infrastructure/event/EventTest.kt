package com.boclips.videos.service.infrastructure.event

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime

class EventTest {


    @Test
    fun `timestamp is normalized to UTC`() {
        val event = Event<Any>("TYPE", ZonedDateTime.of(2018, 1, 1, 10, 0, 0, 0, ZoneOffset.ofHours(10)), "")

        assertThat(event.timestamp).isEqualTo(LocalDateTime.of(2018, 1, 1, 0, 0, 0))
    }
}
