package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.common.model.CursorBasedIndexSearchRequest
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
import com.boclips.search.service.testsupport.VideoQueryFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class VideoIndexReaderCursorIntegrationTest : EmbeddedElasticSearchIntegrationTest() {
    private lateinit var videoIndexReader: VideoIndexReader
    private lateinit var videoIndexWriter: VideoIndexWriter

    @BeforeEach
    fun setUp() {
        videoIndexReader = VideoIndexReader(esClient)
        videoIndexWriter = VideoIndexWriter.createTestInstance(esClient, 20)
    }

    @Test
    @Disabled
    fun `get pages with cursor`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1"),
                SearchableVideoMetadataFactory.create(id = "2"),
                SearchableVideoMetadataFactory.create(id = "3")
            )
        )

        val firstResult = videoIndexReader.search(
            CursorBasedIndexSearchRequest(
                query = VideoQueryFactory.empty(),
                windowSize = 2,
                cursor = null
            )
        )
        assertThat(firstResult.elements).hasSize(2)
        assertNotNull(firstResult.cursor)

        val secondResult = videoIndexReader.search(
            CursorBasedIndexSearchRequest(
                query = VideoQueryFactory.empty(),
                windowSize = 2,
                cursor = firstResult.cursor
            )
        )
        assertThat(secondResult.elements).hasSize(1)
        assertNull(secondResult.cursor)
    }

    @Test
    fun `can handle many cursor requests`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1"),
                SearchableVideoMetadataFactory.create(id = "2"),
                SearchableVideoMetadataFactory.create(id = "3"),
                SearchableVideoMetadataFactory.create(id = "4"),
                SearchableVideoMetadataFactory.create(id = "5"),
                SearchableVideoMetadataFactory.create(id = "6"),
                SearchableVideoMetadataFactory.create(id = "7"),
                SearchableVideoMetadataFactory.create(id = "8"),
                SearchableVideoMetadataFactory.create(id = "9"),
                SearchableVideoMetadataFactory.create(id = "10")
            )
        )

        val firstResult = videoIndexReader.search(
            CursorBasedIndexSearchRequest(
                query = VideoQueryFactory.empty(),
                windowSize = 1,
                cursor = null
            )
        )
        assertThat(firstResult.elements).hasSize(1)
        assertNotNull(firstResult.cursor)

        repeat(9) {
            val result = videoIndexReader.search(
                CursorBasedIndexSearchRequest(
                    query = VideoQueryFactory.empty(),
                    windowSize = 1,
                    cursor = firstResult.cursor
                )
            )
            assertThat(result.elements).hasSize(1)
        }
    }
}
