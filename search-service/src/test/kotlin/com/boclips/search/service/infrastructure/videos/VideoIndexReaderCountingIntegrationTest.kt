package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.videos.model.SubjectMetadata
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.domain.videos.model.VideoType
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest

import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Ignore
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class VideoIndexReaderCountingIntegrationTest : EmbeddedElasticSearchIntegrationTest() {
    private lateinit var videoIndexReader: VideoIndexReader
    private lateinit var videoIndexWriter: VideoIndexWriter

    @BeforeEach
    fun setUp() {
        videoIndexReader = VideoIndexReader(esClient)
        videoIndexWriter = VideoIndexWriter.createTestInstance(esClient, 20)
    }

    @Test
    fun `counts search results for phrase queries`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", description = "Apple banana candy"),
                SearchableVideoMetadataFactory.create(id = "2", description = "candy banana apple"),
                SearchableVideoMetadataFactory.create(id = "3", description = "candy banana apple"),
                SearchableVideoMetadataFactory.create(id = "4", description = "candy banana apple"),
                SearchableVideoMetadataFactory.create(id = "5", description = "candy banana apple"),
                SearchableVideoMetadataFactory.create(id = "6", description = "candy banana apple"),
                SearchableVideoMetadataFactory.create(id = "7", description = "candy banana apple"),
                SearchableVideoMetadataFactory.create(id = "8", description = "candy banana apple"),
                SearchableVideoMetadataFactory.create(id = "9", description = "candy banana apple"),
                SearchableVideoMetadataFactory.create(id = "10", description = "candy banana apple"),
                SearchableVideoMetadataFactory.create(id = "11", description = "candy banana apple")
            )
        )

        val results = videoIndexReader.count(VideoQuery(phrase = "banana"))

        assertThat(results.hits).isEqualTo(11)
    }

    @Test
    fun `counts search results for IDs queries`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "Apple banana candy"),
                SearchableVideoMetadataFactory.create(id = "2", title = "candy banana apple"),
                SearchableVideoMetadataFactory.create(id = "3", title = "banana apple candy")
            )
        )

        val results = videoIndexReader.count(VideoQuery(ids = listOf("2", "5")))

        assertThat(results.hits).isEqualTo(1)
    }

    @Test
    fun `can count by content type`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", type = VideoType.NEWS),
                SearchableVideoMetadataFactory.create(id = "2", type = VideoType.STOCK),
                SearchableVideoMetadataFactory.create(id = "3", type = VideoType.INSTRUCTIONAL),
                SearchableVideoMetadataFactory.create(id = "4", type = VideoType.NEWS),
                SearchableVideoMetadataFactory.create(id = "5", type = VideoType.STOCK)
            )
        )

        val results = videoIndexReader.count(VideoQuery(includedType = setOf(VideoType.NEWS)))

        assertThat(results.hits).isEqualTo(2)
    }

    inner class AggregationCounts {
        fun `returns counts for all subjects`() {
            videoIndexWriter.upsert(
                sequenceOf(
                    SearchableVideoMetadataFactory.create(
                        id = "1", title = "Apple banana candy", subjects = setOf(
                            SubjectMetadata(id = "1", name = "French"),
                            SubjectMetadata(id = "2", name = "Maths")
                        )
                    ),
                    SearchableVideoMetadataFactory.create(
                        id = "2", title = "candy banana apple", subjects = setOf(
                            SubjectMetadata(id = "2", name = "Maths")
                        )
                    ),
                    SearchableVideoMetadataFactory.create(
                        id = "3", title = "banana apple candy", subjects = setOf(
                            SubjectMetadata(id = "3", name = "Literacy")
                        )
                    )
                )
            )

            val counts = videoIndexReader.count(VideoQuery(phrase = "apple"))

            assertThat(counts.hits).isEqualTo(3)

            assertThat(counts.filters?.subjects).hasSize(3)

            assertThat(counts.filters?.subjects?.get(0)?.id).isEqualTo("1")
            assertThat(counts.filters?.subjects?.get(0)?.total).isEqualTo(1)

            assertThat(counts.filters?.subjects?.get(1)?.id).isEqualTo("2")
            assertThat(counts.filters?.subjects?.get(1)?.total).isEqualTo(2)

            assertThat(counts.filters?.subjects?.get(2)?.id).isEqualTo("3")
            assertThat(counts.filters?.subjects?.get(2)?.total).isEqualTo(1)
        }
    }
}
