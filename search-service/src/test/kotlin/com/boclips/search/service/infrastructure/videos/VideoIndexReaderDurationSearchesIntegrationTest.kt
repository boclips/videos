package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration

class VideoIndexReaderDurationSearchesIntegrationTest : EmbeddedElasticSearchIntegrationTest() {
    private lateinit var videoIndexReader: VideoIndexReader
    private lateinit var videoIndexWriter: VideoIndexWriter

    @BeforeEach
    fun setUp() {
        videoIndexReader = VideoIndexReader(esClient)
        videoIndexWriter = VideoIndexWriter(esClient)
    }

    @Test
    fun `duration range matches`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", durationSeconds = 120),
                SearchableVideoMetadataFactory.create(id = "2", durationSeconds = 100),
                SearchableVideoMetadataFactory.create(id = "3", durationSeconds = 60)
            )
        )

        val results =
            videoIndexReader.search(
                PaginatedSearchRequest(
                    query = VideoQuery(
                        minDuration = Duration.ofSeconds(60),
                        maxDuration = Duration.ofSeconds(110)
                    )
                )
            )

        assertThat(results.size).isEqualTo(2)
        assertThat(results).containsExactlyInAnyOrder("2", "3")
    }

    @Test
    fun `duration range no upper bound`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", durationSeconds = 120),
                SearchableVideoMetadataFactory.create(id = "2", durationSeconds = 100),
                SearchableVideoMetadataFactory.create(id = "3", durationSeconds = 40)
            )
        )

        val results =
            videoIndexReader.search(
                PaginatedSearchRequest(
                    query = VideoQuery(
                        minDuration = Duration.ofSeconds(60)
                    )
                )
            )

        assertThat(results.size).isEqualTo(2)
        assertThat(results).containsExactlyInAnyOrder("1", "2")
    }

    @Test
    fun `duration range no lower bound`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", durationSeconds = 120),
                SearchableVideoMetadataFactory.create(id = "2", durationSeconds = 60),
                SearchableVideoMetadataFactory.create(id = "3", durationSeconds = 100)
            )
        )

        val results =
            videoIndexReader.search(
                PaginatedSearchRequest(
                    query = VideoQuery(maxDuration = Duration.ofSeconds(110))
                )
            )

        assertThat(results.size).isEqualTo(2)
        assertThat(results).containsExactlyInAnyOrder("2", "3")
    }
}