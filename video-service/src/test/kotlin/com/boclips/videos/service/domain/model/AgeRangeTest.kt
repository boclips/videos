package com.boclips.videos.service.domain.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AgeRangeTest {

    @Test
    fun `min and max set`() {
        val ageRange = AgeRange.of(min = 3, max = 5, curatedManually = true)

        val specificAgeRange = ageRange as FixedAgeRange
        assertThat(specificAgeRange.min).isEqualTo(3)
        assertThat(specificAgeRange.max).isEqualTo(5)
        assertThat(specificAgeRange.curatedManually).isTrue()
    }

    @Test
    fun `min set, no max set`() {
        val ageRange = AgeRange.of(min = 7, max = null, curatedManually = true)

        val lowerBoundedAgeRange = ageRange as OpenEndedAgeRange
        assertThat(lowerBoundedAgeRange.min).isEqualTo(7)
        assertThat(lowerBoundedAgeRange.curatedManually).isTrue()
    }

    @Test
    fun `min not set, max set`() {
        val ageRange = AgeRange.of(min = null, max = 16, curatedManually = true)

        val upperBoundedAgeRange = ageRange as CappedAgeRange
        assertThat(upperBoundedAgeRange.max).isEqualTo(16)
        assertThat(upperBoundedAgeRange.curatedManually).isTrue()
    }

    @Test
    fun `min and max not set`() {
        val ageRange = AgeRange.of(min = null, max = null, curatedManually = true)

        assertThat(ageRange).isInstanceOf(UnknownAgeRange::class.java)
    }

    @Test
    fun `cannot instantiate an Age Range where min greater or equals max`() {
        assertThrows<IllegalAgeRange> { AgeRange.of(min = 10, max = 8, curatedManually = true) }
        assertThrows<IllegalAgeRange> { AgeRange.of(min = 10, max = 10, curatedManually = true) }
    }
}
