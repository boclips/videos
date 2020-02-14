package com.boclips.contentpartner.service.domain.model

import com.boclips.contentpartner.service.testsupport.TestFactories
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.Test

class AgeRangeBucketsTest {
    @Test
    fun `min of all ranges`() {
        val ranges = AgeRangeBuckets(
            ageRanges = listOf(
                TestFactories.createAgeRange(min = 1),
                TestFactories.createAgeRange(min = 2),
                TestFactories.createAgeRange(min = 3)
            )
        )

        assertThat(ranges.min).isEqualTo(1)
    }

    @Test
    fun `max of all ranges`() {
        val ranges = AgeRangeBuckets(
            ageRanges = listOf(
                TestFactories.createAgeRange(max = 2),
                TestFactories.createAgeRange(max = 1),
                TestFactories.createAgeRange(max = null),
                TestFactories.createAgeRange(max = 3)
            )
        )

        assertThat(ranges.max).isEqualTo(3)
    }

    @Test
    fun `no age ranges gives null min and max values`() {
        val ranges = AgeRangeBuckets(
            ageRanges = emptyList()
        )

        assertThat(ranges.max).isEqualTo(null)
        assertThat(ranges.min).isEqualTo(null)
    }
}
