package com.boclips.videos.service.presentation.event

import com.boclips.videos.service.infrastructure.event.analysis.Interaction
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

import java.time.ZoneOffset
import java.time.ZonedDateTime

class InteractionsFormatterTest {

    @Test
    fun format() {
        val time = ZonedDateTime.of(2018, 2, 3, 10, 11, 12, 0, ZoneOffset.UTC)
        val interactions = listOf(
                Interaction(timestamp = time, description = "root", related = listOf(
                        Interaction(timestamp = time.plusHours(1), description = "nested", related = emptyList())
                )),
                Interaction(timestamp = time.plusHours(2), description = "another", related = emptyList())
        )

        val formatted = InteractionsFormatter.format(interactions)

        assertThat(formatted).isEqualTo(
                """>  2018-02-03T12:11:12Z another
                  |>  2018-02-03T10:11:12Z root
                  |   2018-02-03T11:11:12Z nested
                """.trimMargin())
    }
}
