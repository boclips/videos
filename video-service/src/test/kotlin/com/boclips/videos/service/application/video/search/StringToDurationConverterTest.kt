package com.boclips.videos.service.application.video.search

import com.boclips.videos.service.application.video.exceptions.InvalidDurationException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Duration

internal class StringToDurationConverterTest {
    private val durationConverter: StringToDurationConverter = StringToDurationConverter()

    @Test
    fun `Converting null, returns a null`() {
        val actual = durationConverter.convertToDuration(null)

        assertThat(actual).isNull()
    }

    @Test
    fun `Converting empty string, returns a null`() {
        val actual = durationConverter.convertToDuration("")

        assertThat(actual).isNull()
    }

    @Test
    fun `converts a valid iso string to a duration`() {
        val duration = durationConverter.convertToDuration(Duration.ofSeconds(5).toString())

        assertThat(duration).isEqualTo(Duration.ofSeconds(5))
    }

    @Test
    fun `throws an exception on an invalid ISO Duration`() {
        assertThrows<InvalidDurationException> {
            durationConverter.convertToDuration("Testing123")
        }
    }
}