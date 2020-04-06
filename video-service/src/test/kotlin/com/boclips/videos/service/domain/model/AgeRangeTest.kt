package com.boclips.videos.service.domain.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AgeRangeTest {

    @Test
    fun `min and max set`() {
        val ageRange = AgeRange.of(min = 3, max = 5)

        val specificAgeRange = ageRange as FixedAgeRange
        assertThat(specificAgeRange.min).isEqualTo(3)
        assertThat(specificAgeRange.max).isEqualTo(5)
    }

    @Test
    fun `min set, no max set`() {
        val ageRange = AgeRange.of(min = 7, max = null)

        val lowerBoundedAgeRange = ageRange as OpenEndedAgeRange
        assertThat(lowerBoundedAgeRange.min).isEqualTo(7)
    }

    @Test
    fun `min not set, max set`() {
        val ageRange = AgeRange.of(min = null, max = 16)

        val upperBoundedAgeRange = ageRange as CappedAgeRange
        assertThat(upperBoundedAgeRange.max).isEqualTo(16)
    }

    @Test
    fun `min and max not set`() {
        val ageRange = AgeRange.of(min = null, max = null)

        assertThat(ageRange).isInstanceOf(UnknownAgeRange::class.java)
    }
}
