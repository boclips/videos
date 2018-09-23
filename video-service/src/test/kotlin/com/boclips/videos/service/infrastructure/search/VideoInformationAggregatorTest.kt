package com.boclips.videos.service.infrastructure.search

import com.boclips.kalturaclient.MediaEntry
import com.boclips.kalturaclient.streams.StreamUrls
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.Duration
import java.time.LocalDate

class VideoInformationAggregatorTest {

    @Test
    fun `convert a list of elastic search videos and a list of media entries to a list of domain videos`() {
        val elasticSearchVideo = TestFactories.createElasticSearchVideos(id = "video-id", referenceId = "ref-id")
        val mediaEntry = MediaEntry.builder()
                .referenceId("ref-id")
                .streams(StreamUrls("http://template/[FORMAT]"))
                .thumbnailUrl("http://thumbnail")
                .duration(Duration.ofMinutes(1).plusSeconds(24))
                .build()

        val videos = VideoInformationAggregator.convert(listOf(elasticSearchVideo), mapOf("ref-id" to mediaEntry))

        assertThat(videos).hasSize(1)
        assertThat(videos[0].id).isEqualTo("video-id")
        assertThat(videos[0].title).isEqualTo("video-title")
        assertThat(videos[0].description).isEqualTo("video-description")
        assertThat(videos[0].duration).isEqualTo(Duration.ofSeconds(84))
        assertThat(videos[0].releasedOn).isEqualTo(LocalDate.of(2018, 1, 2))
        assertThat(videos[0].contentProvider).isEqualTo("video-source")
        assertThat(videos[0].streamUrl).isEqualTo("http://template/mpegdash")
        assertThat(videos[0].thumbnailUrl).isEqualTo("http://thumbnail")
    }

    @Test
    fun `conversion skips items when there is no media entry`() {
        val elasticSearchVideo = TestFactories.createElasticSearchVideos(id = "video-id")

        val videos = VideoInformationAggregator.convert(listOf(elasticSearchVideo), emptyMap())

        assertThat(videos).isEmpty()
    }


}
