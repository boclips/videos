package com.boclips.contentpartner.service.domain.model

import com.boclips.videos.api.request.contentpartner.AgeRangeRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AgeRangeComparatorTest {
    @Test
    fun `returns true if the new age request is higher and bounded`() {
        val areDifferent = AgeRangeComparator.areDifferent(AgeRange.bounded(3, 5), AgeRangeRequest(4, 6))

        assertThat(areDifferent).isTrue()
    }

    @Test
    fun `returns true if the new age request is lower and bounded`() {
        val areDifferent = AgeRangeComparator.areDifferent(AgeRange.bounded(4, 7), AgeRangeRequest(3, 6))

        assertThat(areDifferent).isTrue()
    }

    @Test
    fun `returns true if new age request is upper-unbounded`() {
        val areDifferent = AgeRangeComparator.areDifferent(AgeRange.bounded(4, 7), AgeRangeRequest(4, null))

        assertThat(areDifferent).isTrue()
    }

    @Test
    fun `returns false if the age request is the same`() {
        val areDifferent = AgeRangeComparator.areDifferent(AgeRange.bounded(4, 7), AgeRangeRequest(4, 7))

        assertThat(areDifferent).isFalse()
    }
}