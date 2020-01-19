package com.boclips.videos.service.application.video.search

import com.boclips.search.service.domain.videos.model.SourceType
import com.boclips.videos.service.application.video.exceptions.InvalidDateException
import com.boclips.videos.service.application.video.exceptions.InvalidDurationException
import com.boclips.videos.service.application.video.exceptions.InvalidSourceException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Duration
import java.time.LocalDate

internal class SearchVideoQueryConverterTest {
    private val searchQueryConverter: SearchQueryConverter = SearchQueryConverter()

    @Nested
    inner class DurationTests {
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
    }

    @Nested
    inner class SourceTests {
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

    @Nested
    inner class ReleasedDateTests {
        @Test
        fun `Converting null, returns a null`() {
            val date = searchQueryConverter.convertDate(null)

            assertThat(date).isNull()
        }

        @Test
        fun `Converting empty string, returns a null`() {
            val date = searchQueryConverter.convertDate("")

            assertThat(date).isNull()
        }

        @Test
        fun `converts a valid YYYY-MM-DD iso string to a date`() {
            val date = searchQueryConverter.convertDate(LocalDate.of(2011, 1, 1).toString())

            assertThat(date).isEqualTo(LocalDate.of(2011, 1, 1))
        }

        @Test
        fun `throws an exception on an invalid ISO date`() {
            assertThrows<InvalidDateException> {
                searchQueryConverter.convertDate("Testing123")
            }
        }
    }
}
