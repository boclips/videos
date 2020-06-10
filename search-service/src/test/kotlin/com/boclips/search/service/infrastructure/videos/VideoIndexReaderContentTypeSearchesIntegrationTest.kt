package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.domain.videos.model.VideoType
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest

import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class VideoIndexReaderContentTypeSearchesIntegrationTest : EmbeddedElasticSearchIntegrationTest() {
    private lateinit var videoIndexReader: VideoIndexReader
    private lateinit var videoIndexWriter: VideoIndexWriter

    @BeforeEach
    fun setUp() {
        videoIndexReader = VideoIndexReader(esClient)
        videoIndexWriter = VideoIndexWriter.createTestInstance(esClient, 20)
    }

    @Test
    fun `returns documents with specified content types`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", types = listOf(VideoType.NEWS)),
                SearchableVideoMetadataFactory.create(id = "2", types = listOf(VideoType.STOCK)),
                SearchableVideoMetadataFactory.create(id = "3", types = listOf(VideoType.INSTRUCTIONAL)),
                SearchableVideoMetadataFactory.create(id = "4", types = listOf(VideoType.NEWS)),
                SearchableVideoMetadataFactory.create(id = "5", types = listOf(VideoType.STOCK))
            )
        )

        val results = videoIndexReader.search(
            PaginatedSearchRequest(
                VideoQuery(
                    includedType = setOf(
                        VideoType.STOCK,
                        VideoType.INSTRUCTIONAL
                    )
                ), 0, 10
            )
        )

        assertThat(results.elements).containsOnly("2", "3", "5")
    }

    @Test
    fun `returns all documents when given no content types to filter by`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1"),
                SearchableVideoMetadataFactory.create(id = "2"),
                SearchableVideoMetadataFactory.create(id = "3", types = listOf(VideoType.INSTRUCTIONAL)),
                SearchableVideoMetadataFactory.create(id = "4", types = listOf(VideoType.NEWS)),
                SearchableVideoMetadataFactory.create(id = "5")
            )
        )

        val results = videoIndexReader.search(
            PaginatedSearchRequest(
                VideoQuery(
                    includedType = emptySet()
                ), 0, 10
            )
        )

        assertThat(results.elements).containsOnly("1", "2", "3", "4", "5")
    }

    @Test
    fun `excluded types take precedence`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", types = listOf(VideoType.NEWS)),
                SearchableVideoMetadataFactory.create(id = "2", types = listOf(VideoType.STOCK)),
                SearchableVideoMetadataFactory.create(id = "3", types = listOf(VideoType.INSTRUCTIONAL)),
                SearchableVideoMetadataFactory.create(id = "4", types = listOf(VideoType.NEWS)),
                SearchableVideoMetadataFactory.create(id = "5", types = listOf(VideoType.STOCK))
            )
        )

        val results = videoIndexReader.search(
            PaginatedSearchRequest(
                VideoQuery(
                    includedType = setOf(
                        VideoType.STOCK,
                        VideoType.INSTRUCTIONAL
                    ),
                    excludedType = setOf(
                        VideoType.STOCK
                    )
                ), 0, 10
            )
        )

        assertThat(results.elements).containsOnly("3")
    }

    @Test
    fun `excluded types can be combined with phrase queries`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "Wild Rhino", types = listOf(VideoType.NEWS)),
                SearchableVideoMetadataFactory.create(id = "2", title = "Domesticated Rhino", types = listOf(VideoType.NEWS)),
                SearchableVideoMetadataFactory.create(id = "3", title = "Cyborg Rhino", types = listOf(VideoType.STOCK))
            )
        )

        val results = videoIndexReader.search(
            PaginatedSearchRequest(
                VideoQuery(
                    phrase = "Rhino",
                    excludedType = setOf(
                        VideoType.NEWS
                    )
                ), 0, 10
            )
        )

        assertThat(results.elements).containsOnly("3")
    }
}
