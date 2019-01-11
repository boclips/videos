package com.boclips.search.service.infrastructure.legacy

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Duration

class LegacyDurationFormatterTest {

    @Test
    fun `less than a minute`() {
        assertThat(LegacyDurationFormatter.format(Duration.ofSeconds(38))).isEqualTo("00:00:38")
    }

    @Test
    fun `more than a minute`() {
        assertThat(LegacyDurationFormatter.format(Duration.ofMinutes(10).plusSeconds(38))).isEqualTo("00:10:38")
    }

    @Test
    fun `more than an hour`() {
        assertThat(LegacyDurationFormatter.format(Duration.ofHours(5).plusMinutes(4).plusSeconds(1))).isEqualTo("05:04:01")
    }

    @Test
    fun `more than 100 hour`() {
        assertThat(LegacyDurationFormatter.format(Duration.ofHours(100).plusMinutes(0).plusSeconds(0))).isEqualTo("100:00:00")
    }
}