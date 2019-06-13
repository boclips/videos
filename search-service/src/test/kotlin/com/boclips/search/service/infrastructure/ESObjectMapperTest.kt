package com.boclips.search.service.infrastructure

import com.boclips.search.service.infrastructure.videos.ESVideo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.Month

class ESObjectMapperTest {

    @Test
    fun `writes dates as strings in ES default format`() {
        val serialisedVideo = ESObjectMapper.get().writeValueAsString(
            ESVideo(
                id = "1",
                title = "title",
                description = "description",
                contentProvider = "contentProvider",
                keywords = listOf("keywords"),
                tags = listOf("tags"),
                releaseDate = LocalDate.of(2015, Month.MAY, 21),
                durationSeconds = 10,
                source = "Boclips",
                transcript = null,
                ageRangeMin = 3,
                ageRangeMax = 11
            )
        )

        assertThat(serialisedVideo).contains("2015-05-21")
    }
}
