package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class VideoIndexReaderIsClassroomIntegrationTest : EmbeddedElasticSearchIntegrationTest() {
    private lateinit var videoIndexReader: VideoIndexReader
    private lateinit var videoIndexWriter: VideoIndexWriter

    @BeforeEach
    fun setUp() {
        videoIndexReader = VideoIndexReader(esClient)
        videoIndexWriter = VideoIndexWriter.createTestInstance(esClient)
    }

    @Test
    fun `can filter out non classroom videos`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    description = "banana",
                    isClassroom = true
                ), SearchableVideoMetadataFactory.create(
                    id = "3",
                    description = "banana",
                    isClassroom = false
                )
            )
        )

        val results = videoIndexReader.search(
            PaginatedSearchRequest(query = VideoQuery(isClassroom = true))
        )

        assertThat(results).containsExactly("1")
    }

    @Test
    fun `can filter out classroom videos`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    description = "banana",
                    isClassroom = true
                ), SearchableVideoMetadataFactory.create(
                    id = "3",
                    description = "banana",
                    isClassroom = false
                )
            )
        )

        val results = videoIndexReader.search(
            PaginatedSearchRequest(query = VideoQuery(isClassroom = false))
        )

        assertThat(results).containsExactly("3")
    }


    @Test
    fun `does not filter when classroom filter is not specified`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    description = "banana",
                    isClassroom = true
                ), SearchableVideoMetadataFactory.create(
                    id = "3",
                    description = "banana",
                    isClassroom = false
                )
            )
        )

        val results = videoIndexReader.search(
            PaginatedSearchRequest(query = VideoQuery())
        )

        assertThat(results).containsExactlyInAnyOrder("1", "3")
    }
}
