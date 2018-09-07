package com.boclips.videos.service.infrastructure.search

import com.boclips.videos.service.domain.model.Video
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.Duration
import java.time.LocalDate
import java.time.Month

class ElasticSearchVideoTest {

    @Test
    fun `toVideo parses video`() {
        val elasticSearchVideo = ElasticSearchVideo(
                id = "id",
                title = "title",
                source = "source",
                date = "2000-01-03",
                duration = "21:12:01",
                description = "description"
        )

        assertThat(elasticSearchVideo.toVideo()).isEqualTo(Video(
                id = "id",
                title = "title",
                contentProvider = "source",
                releasedOn = LocalDate.of(2000, Month.JANUARY, 3),
                duration = Duration.parse("PT21H12M1S"),
                description = "description"
        ))
    }
    @Test
    fun `toVideo when wrong duration sets zero`() {
        val elasticSearchVideo = ElasticSearchVideo(
                id = "id",
                title = "title",
                source = "source",
                date = "2000-01-03",
                duration = "01",
                description = "description"
        )

        assertThat(elasticSearchVideo.toVideo()).isEqualTo(Video(
                id = "id",
                title = "title",
                contentProvider = "source",
                releasedOn = LocalDate.of(2000, Month.JANUARY, 3),
                duration = Duration.ZERO,
                description = "description"
        ))
    }
}