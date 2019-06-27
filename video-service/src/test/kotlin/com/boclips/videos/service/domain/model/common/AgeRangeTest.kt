package com.boclips.videos.service.domain.model.common

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class AgeRangeTest {

    @Test
    fun `bounded age range`() {
        val ageRange = AgeRange.bounded(3, 5)

        assertThat(ageRange.min()).isEqualTo(3)
        assertThat(ageRange.max()).isEqualTo(5)
    }

    @Test
    fun `bounded age range with no max`() {
        val ageRange = AgeRange.bounded(7, null)

        assertThat(ageRange.min()).isEqualTo(7)
        assertThat(ageRange.max()).isNull()
    }

    @Test
    fun `unbounded age range`() {
        val ageRange = AgeRange.unbounded()

        assertThat(ageRange.min()).isNull()
        assertThat(ageRange.max()).isNull()
    }
}
