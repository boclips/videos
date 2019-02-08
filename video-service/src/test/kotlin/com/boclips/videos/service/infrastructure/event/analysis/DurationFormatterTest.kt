package com.boclips.videos.service.infrastructure.event.analysis

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class DurationFormatterTest {

    @Test
    fun `returns just seconds when less then a minute`() {
        assertThat(DurationFormatter.formatSeconds(59)).isEqualTo("59s")
    }

    @Test
    fun `returns minutes and seconds when a minute or more`() {
        assertThat(DurationFormatter.formatSeconds(69)).isEqualTo("1m 9s")
    }
}
