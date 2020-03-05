package com.boclips.search.service.domain.videos.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AgeRangeTest {
    @Test
    fun `converts to a range`() {
        val ageRange = AgeRange(min = 5, max = 11)

        assertThat(ageRange.toRange()).isEqualTo((5..11).toList())
    }

    @Test
    fun `converts to a range with default max`() {
        val ageRange = AgeRange(min = 5)

        assertThat(ageRange.toRange()).isEqualTo((5..99).toList())
    }

    @Test
    fun `converts to a range with default min`() {
        val ageRange = AgeRange(max = 11)

        assertThat(ageRange.toRange()).isEqualTo((3..11).toList())
    }
}
