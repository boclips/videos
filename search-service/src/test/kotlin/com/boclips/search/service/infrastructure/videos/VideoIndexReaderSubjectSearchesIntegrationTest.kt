package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
import com.boclips.search.service.testsupport.TestFactories.createSubjectMetadata
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class VideoIndexReaderSubjectSearchesIntegrationTest : EmbeddedElasticSearchIntegrationTest() {
    private lateinit var videoIndexReader: VideoIndexReader
    private lateinit var videoIndexWriter: VideoIndexWriter

    @BeforeEach
    fun setUp() {
        videoIndexReader = VideoIndexReader(esClient)
        videoIndexWriter = VideoIndexWriter.createTestInstance(esClient)
    }

    @Test
    fun `filtering by subject when there is a match`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    title = "TED",
                    subjects = setOf(createSubjectMetadata("subject-one"), createSubjectMetadata("subject-two"))
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    title = "TED",
                    subjects = setOf(createSubjectMetadata("subject-two"), createSubjectMetadata("subject-three"))
                ),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    title = "TED",
                    subjects = setOf(createSubjectMetadata("subject-three"))
                )
            )
        )

        val results = videoIndexReader.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    subjectIds = setOf("subject-two")
                )
            )
        )

        assertThat(results).hasSize(2)
        assertThat(results).contains("1")
        assertThat(results).contains("2")
    }

    @Test
    fun `filtering by subject when there is no match`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    title = "TED",
                    subjects = setOf(createSubjectMetadata("maths-123"))
                )
            )
        )

        val results = videoIndexReader.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    phrase = "",
                    subjectIds = setOf("biology-987")
                )
            )
        )

        assertThat(results).hasSize(0)
    }

    @Test
    fun `boosting matching user subjects`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    title = "TED",
                    subjects = setOf(createSubjectMetadata("subject-one"))
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    title = "TED",
                    subjects = setOf(createSubjectMetadata("subject-two"))
                ),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    title = "TED",
                    subjects = setOf(createSubjectMetadata("subject-three"))
                ),
                SearchableVideoMetadataFactory.create(
                    id = "4",
                    title = "TED",
                    subjects = setOf(createSubjectMetadata("subject-two"), createSubjectMetadata("subject-three"))
                )
            )
        )

        val results = videoIndexReader.search(
            PaginatedSearchRequest(
                query = VideoQuery(phrase = "ted", userSubjectIds = setOf("subject-two", "subject-four"))
            )
        )

        assertThat(results).hasSize(4)
        assertThat(results.subList(0, 2)).containsExactlyInAnyOrder("2", "4")
    }
}
