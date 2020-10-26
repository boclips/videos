package com.boclips.videos.service.application.video.search

import com.boclips.search.service.domain.videos.model.DurationRange
import com.boclips.search.service.domain.videos.model.SourceType
import com.boclips.videos.service.application.common.QueryConverter
import com.boclips.videos.service.application.video.exceptions.InvalidDateException
import com.boclips.videos.service.application.video.exceptions.InvalidDurationException
import com.boclips.videos.service.application.video.exceptions.InvalidSourceException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Duration
import java.time.LocalDate

class QueryConverterTest {

    private val queryConverter: QueryConverter = QueryConverter()

    @Nested
    inner class DurationTests {
        @Test
        fun `Converting null, returns a null`() {
            val actual = queryConverter.convertDuration(null)

            assertThat(actual).isNull()
        }

        @Test
        fun `Converting empty string, returns a null`() {
            val actual = queryConverter.convertDuration("")

            assertThat(actual).isNull()
        }

        @Test
        fun `converts a valid iso string to a duration`() {
            val duration = queryConverter.convertDuration(Duration.ofSeconds(5).toString())

            assertThat(duration).isEqualTo(Duration.ofSeconds(5))
        }

        @Test
        fun `throws an exception on an invalid ISO Duration`() {
            assertThrows<InvalidDurationException> {
                queryConverter.convertDuration("Testing123")
            }
        }
    }

    @Nested
    inner class SourceTests {
        @Test
        fun `converting null to source type`() {
            val sourceType = queryConverter.convertSource(null)

            assertThat(sourceType).isNull()
        }

        @Test
        fun `converting youtube string to source type`() {
            val sourceType = queryConverter.convertSource("youtube")

            assertThat(sourceType).isEqualTo(SourceType.YOUTUBE)
        }

        @Test
        fun `converting boclips string to source type`() {
            val sourceType = queryConverter.convertSource("boclips")

            assertThat(sourceType).isEqualTo(SourceType.BOCLIPS)
        }

        @Test
        fun `converting an invalid string to source type`() {
            assertThrows<InvalidSourceException> {
                queryConverter.convertSource("elephants")
            }
        }
    }

    @Nested
    inner class ReleasedDateTests {
        @Test
        fun `Converting null, returns a null`() {
            val date = queryConverter.convertDate(null)

            assertThat(date).isNull()
        }

        @Test
        fun `Converting empty string, returns a null`() {
            val date = queryConverter.convertDate("")

            assertThat(date).isNull()
        }

        @Test
        fun `converts a valid YYYY-MM-DD iso string to a date`() {
            val date = queryConverter.convertDate(LocalDate.of(2011, 1, 1).toString())

            assertThat(date).isEqualTo(LocalDate.of(2011, 1, 1))
        }

        @Test
        fun `throws an exception on an invalid ISO date`() {
            assertThrows<InvalidDateException> {
                queryConverter.convertDate("Testing123")
            }
        }
    }

    @Nested
    inner class ConvertDuration {
        @Test
        fun `it returns an empty list when no durations are provided`() {
            val queryConverter = QueryConverter()
            val actual = queryConverter.convertDurations(null, null, null)
            assertThat(actual).isEmpty()
        }

        @Test
        fun `it returns a single duration range when durationMin and durationMax are provided`() {
            val queryConverter = QueryConverter()
            val actual = queryConverter.convertDurations("PT2M", "PT5M", null)
            assertThat(actual).contains(DurationRange(min = Duration.ofMinutes(2), max = Duration.ofMinutes(5)))
        }

        @Test
        fun `it returns a single duration range when only durationMin is provided`() {
            val queryConverter = QueryConverter()
            val actual = queryConverter.convertDurations("PT2M", null, null)
            assertThat(actual).contains(DurationRange(min = Duration.ofMinutes(2), max = null))
        }

        @Test
        fun `it returns a single duration range when only durationMax is provided`() {
            val queryConverter = QueryConverter()
            val actual = queryConverter.convertDurations(null, "PT5M", null)
            assertThat(actual).contains(DurationRange(min = Duration.ofMinutes(0), max = Duration.ofMinutes(5)))
        }

        @Test
        fun `it returns a list of multiple duration ranges when duration is provided`() {
            val queryConverter = QueryConverter()
            val actual = queryConverter.convertDurations(null, null, listOf("PT0M-PT2M", "PT2M-PT5M"))
            assertThat(actual).isEqualTo(
                listOf(
                    DurationRange(min = Duration.ofMinutes(0), max = Duration.ofMinutes(2)),
                    DurationRange(min = Duration.ofMinutes(2), max = Duration.ofMinutes(5))
                )
            )
        }

        @Test
        fun `it handles a lower bound only duration`() {
            val queryConverter = QueryConverter()
            val actual = queryConverter.convertDurations(null, null, listOf("PT5M"))
            assertThat(actual).isEqualTo(
                listOf(
                    DurationRange(min = Duration.ofMinutes(5), max = null)
                )
            )
        }

        @Test
        fun `it prefers durations over min and max parameters`() {
            val queryConverter = QueryConverter()
            val actual = queryConverter.convertDurations("PT10M", "PT11M", listOf("PT5M"))
            assertThat(actual).isEqualTo(
                listOf(
                    DurationRange(min = Duration.ofMinutes(5), max = null)
                )
            )
        }
    }
}
