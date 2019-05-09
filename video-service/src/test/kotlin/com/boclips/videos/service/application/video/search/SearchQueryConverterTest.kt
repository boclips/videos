package com.boclips.videos.service.application.video.search

import com.boclips.search.service.domain.SourceType
import com.boclips.videos.service.application.video.exceptions.InvalidDurationException
import com.boclips.videos.service.application.video.exceptions.InvalidSourceException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Duration

internal class SearchQueryConverterTest {
    private val searchQueryConverter: SearchQueryConverter = SearchQueryConverter()

    @Test
    fun `Converting null, returns a null`() {
        val actual = searchQueryConverter.convertDuration(null)

        assertThat(actual).isNull()
    }

    @Test
    fun `Converting empty string, returns a null`() {
        val actual = searchQueryConverter.convertDuration("")

        assertThat(actual).isNull()
    }

    @Test
    fun `converts a valid iso string to a duration`() {
        val duration = searchQueryConverter.convertDuration(Duration.ofSeconds(5).toString())

        assertThat(duration).isEqualTo(Duration.ofSeconds(5))
    }

    @Test
    fun `throws an exception on an invalid ISO Duration`() {
        assertThrows<InvalidDurationException> {
            searchQueryConverter.convertDuration("Testing123")
        }
    }

    @Test
    fun `converting null to source type`() {
        val sourceType = searchQueryConverter.convertSource(null)

        assertThat(sourceType).isNull()
    }

    @Test
    fun `converting youtube string to source type`() {
        val sourceType = searchQueryConverter.convertSource("youtube")

        assertThat(sourceType).isEqualTo(SourceType.YOUTUBE)
    }
    @Test
    fun `converting boclips string to source type`() {
        val sourceType = searchQueryConverter.convertSource("boclips")

        assertThat(sourceType).isEqualTo(SourceType.BOCLIPS)
    }

    @Test
    fun `converting an invalid string to source type`() {
        assertThrows<InvalidSourceException> {
            searchQueryConverter.convertSource("elephants")
        }
    }


}