package com.boclips.search.service.infrastructure

import com.boclips.search.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.Month
import java.time.ZonedDateTime

class ESObjectMapperTest {

    @Test
    fun `writes dates as strings in ES default format`() {
        val serialisedVideo = ESObjectMapper.get().writeValueAsString(
            TestFactories.createVideoDocument(
                releaseDate = LocalDate.of(2015, Month.MAY, 21),
                ingestDate = ZonedDateTime.parse("2017-04-24T09:30Z[UTC]")
            )
        )

        assertThat(serialisedVideo).contains("2015-05-21")
        assertThat(serialisedVideo).contains("2017-04-24")
    }
}
