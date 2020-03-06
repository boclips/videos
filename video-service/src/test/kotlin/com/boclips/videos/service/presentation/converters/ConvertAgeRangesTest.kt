package com.boclips.videos.service.presentation.converters

import com.boclips.videos.service.application.exceptions.InvalidAgeRangeFormatException
import com.boclips.videos.service.domain.model.AgeRange
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows

class ConvertAgeRangesTest {
    @Test
    fun `converts a valid age range`() {
        assertThat(convertAgeRanges("3-7")).isEqualTo(AgeRange.bounded(3, 7))
    }

    @Test
    fun `throws for an invalid age range`() {
        assertThrows<InvalidAgeRangeFormatException>{ convertAgeRanges("abc") }
    }
}
