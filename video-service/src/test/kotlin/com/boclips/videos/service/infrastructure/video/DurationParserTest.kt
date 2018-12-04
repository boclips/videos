package com.boclips.videos.service.infrastructure.video

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Duration

class DurationParserTest {

    @Test
    fun `can parse durations formatted with colons and two digit segments`() {
        assertThat(DurationParser.parse("01:11:13")).isEqualTo(Duration.ofHours(1).plusMinutes(11).plusSeconds(13))
    }

    @Test
    fun `returns zero duration when segments don't have two digits`() {
        assertThat(DurationParser.parse("1:2:3")).isEqualTo(Duration.ZERO)
    }

    @Test
    fun `returns zero duration when not in correct format`() {
        assertThat(DurationParser.parse("booooom")).isEqualTo(Duration.ZERO)
    }

    @Test
    fun `returns zero duration when null`() {
        assertThat(DurationParser.parse(null)).isEqualTo(Duration.ZERO)
    }

    @Test
    fun `returns zero duration when minutes or seconds are greater than 59`() {
        assertThat(DurationParser.parse("00:60:00")).isEqualTo(Duration.ZERO)
        assertThat(DurationParser.parse("00:00:60")).isEqualTo(Duration.ZERO)
    }
}