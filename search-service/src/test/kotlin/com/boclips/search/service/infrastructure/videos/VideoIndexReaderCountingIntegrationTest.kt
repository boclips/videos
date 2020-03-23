package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.common.Bucket
import com.boclips.search.service.domain.common.Count
import com.boclips.search.service.domain.videos.model.AgeRange
import com.boclips.search.service.domain.videos.model.SubjectMetadata
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.domain.videos.model.VideoType
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest

import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
import org.assertj.core.api.Assertions.assertThat
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

    @Nested
    inner class AggregationCounts {
        @Test
        fun `returns counts for all subjects without filter`() {
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
            assertThat(counts.getCounts(Bucket.SubjectsBucket)).hasSize(3)

            assertThat(counts.getCounts(Bucket.SubjectsBucket)).contains(Count(id = "1", hits = 1))
            assertThat(counts.getCounts(Bucket.SubjectsBucket)).contains(Count(id = "2", hits = 2))
            assertThat(counts.getCounts(Bucket.SubjectsBucket)).contains(Count(id = "3", hits = 1))
        }

        @Test
        fun `returns counts for all subjects with filter`() {
            videoIndexWriter.upsert(
                sequenceOf(
                    SearchableVideoMetadataFactory.create(
                        id = "1", title = "Apple banana candy", subjects = setOf(
                            SubjectMetadata(id = "1", name = "French")
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

            val counts = videoIndexReader.count(VideoQuery(phrase = "apple", subjectIds = setOf("1")))

            assertThat(counts.hits).isEqualTo(1)
            assertThat(counts.getCounts(Bucket.SubjectsBucket)).hasSize(3)

            assertThat(counts.getCounts(Bucket.SubjectsBucket)).contains(Count(id = "1", hits = 1))
            assertThat(counts.getCounts(Bucket.SubjectsBucket)).contains(Count(id = "2", hits = 1))
            assertThat(counts.getCounts(Bucket.SubjectsBucket)).contains(Count(id = "3", hits = 1))
        }

        @Test
        fun `returns counts subjects when another filter is applied`() {
            videoIndexWriter.upsert(
                sequenceOf(
                    SearchableVideoMetadataFactory.create(
                        id = "1", title = "Apple banana candy", subjects = setOf(
                            SubjectMetadata(id = "1", name = "French"),
                            SubjectMetadata(id = "2", name = "Maths")
                        ),
                        ageRangeMax = 3, ageRangeMin = 1
                    ),
                    SearchableVideoMetadataFactory.create(
                        id = "2", title = "candy banana apple", subjects = setOf(
                            SubjectMetadata(id = "2", name = "Maths")
                        ),
                        ageRangeMin = 13, ageRangeMax = 18
                    ),
                    SearchableVideoMetadataFactory.create(
                        id = "3", title = "banana apple candy", subjects = setOf(
                            SubjectMetadata(id = "3", name = "Literacy")
                        ),
                        ageRangeMin = 13,
                        ageRangeMax = 17
                    )
                )
            )

            val counts = videoIndexReader.count(
                VideoQuery(phrase = "apple", subjectIds = setOf("1"), ageRanges = listOf(AgeRange(1, 3)))
            )

            assertThat(counts.hits).isEqualTo(1)
            assertThat(counts.getCounts(Bucket.SubjectsBucket)).hasSize(2)

            assertThat(counts.getCounts(Bucket.SubjectsBucket)).contains(Count(id = "1", hits = 1))
            assertThat(counts.getCounts(Bucket.SubjectsBucket)).contains(Count(id = "2", hits = 1))
        }
    }
}
