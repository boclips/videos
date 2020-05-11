package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class VideoIndexReaderAttachmentTypeIntegrationTest : EmbeddedElasticSearchIntegrationTest() {
    private lateinit var videoIndexReader: VideoIndexReader
    private lateinit var videoIndexWriter: VideoIndexWriter

    @BeforeEach
    fun setUp() {
        videoIndexReader = VideoIndexReader(esClient)
        videoIndexWriter = VideoIndexWriter.createTestInstance(esClient, 20)
    }

    @Test
    fun `queries by attachment type`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    title = "Apple banana candy",
                    durationSeconds = 70,
                    attachmentTypes = setOf("Activity")
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    title = "Banana apple",
                    durationSeconds = 130,
                    attachmentTypes = setOf("Lesson Guide")
                ),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    title = "Apple candy",
                    durationSeconds = 310
                )
            )
        )

        val results = videoIndexReader.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    attachmentTypes = setOf("Lesson Guide")
                )
            )
        )
        assertThat(results.counts.totalHits).isEqualTo(1)
        assertThat(results.elements).containsExactly("2")
    }
}