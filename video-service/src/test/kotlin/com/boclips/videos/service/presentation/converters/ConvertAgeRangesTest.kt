package com.boclips.videos.service.presentation.converters

import com.boclips.videos.service.application.exceptions.InvalidAgeRangeFormatException
import com.boclips.videos.service.domain.model.AgeRange
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ConvertAgeRangesTest {
    @Test
    fun `converts a valid age range`() {
        assertThat(convertAgeRanges("3-7")).isEqualTo(AgeRange.of(min = 3, max = 7, curatedManually = false))
    }

    @Test
    fun `throws for an invalid age range`() {
        assertThrows<InvalidAgeRangeFormatException> { convertAgeRanges("abc") }
    }
}
