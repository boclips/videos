package com.boclips.videos.service.presentation.event

import com.boclips.videos.service.infrastructure.event.types.User
import com.boclips.videos.service.testsupport.TestFactories.createInteraction
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.ZoneOffset
import java.time.ZonedDateTime

class InteractionsFormatterTest {

    @Test
    fun format() {
        val time = ZonedDateTime.of(2018, 2, 3, 10, 11, 12, 0, ZoneOffset.UTC)
        val interactions = listOf(
                createInteraction(timestamp = time, description = "root", user = User(false), related = listOf(
                        createInteraction(timestamp = time.plusHours(1), description = "nested", related = emptyList())
                )),
                createInteraction(timestamp = time.plusHours(2), description = "another", user = User(true), related = emptyList())
        )

        val formatted = InteractionsFormatter.format(interactions)

        assertThat(formatted).isEqualTo(
                """>  2018-02-03T12:11:12Z another by Boclips employee
                  |>  2018-02-03T10:11:12Z root by teacher
                  |   2018-02-03T11:11:12Z nested
                """.trimMargin())
    }
}
