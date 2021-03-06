package com.boclips.search.service.domain

import com.boclips.search.service.domain.videos.model.AgeRange
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AgeRangeTest {
    @Test
    fun `converts to a range`() {
        val ageRange = AgeRange(min = 5, max = 11)

        assertThat(ageRange.toRange()).isEqualTo(listOf(5, 6, 7, 8, 9, 10, 11))
    }

    @Test
    fun `converts to a range with default max`() {
        val ageRange = AgeRange(min = 5)

        assertThat(ageRange.toRange()).isEqualTo((5..99).toList())
    }

    @Test
    fun `converts to a range with default min`() {
        val ageRange = AgeRange(max = 11)

        assertThat(ageRange.toRange()).isEqualTo(listOf(3, 4, 5, 6, 7, 8, 9, 10, 11))
    }
}
