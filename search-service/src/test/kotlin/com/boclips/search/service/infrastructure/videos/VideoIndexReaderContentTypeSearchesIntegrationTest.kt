package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.domain.videos.model.VideoType
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.ReindexPropertiesFactory
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
        videoIndexWriter = VideoIndexWriter.createTestInstance(esClient, ReindexPropertiesFactory.create())
    }

    @Test
    fun `returns documents with specified content types`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", type = VideoType.NEWS),
                SearchableVideoMetadataFactory.create(id = "2", type = VideoType.STOCK),
                SearchableVideoMetadataFactory.create(id = "3", type = VideoType.INSTRUCTIONAL),
                SearchableVideoMetadataFactory.create(id = "4", type = VideoType.NEWS),
                SearchableVideoMetadataFactory.create(id = "5", type = VideoType.STOCK)
            )
        )

        val documentIds = videoIndexReader.search(
            PaginatedSearchRequest(
                VideoQuery(
                    includedType = setOf(
                        VideoType.STOCK,
                        VideoType.INSTRUCTIONAL
                    )
                ), 0, 10
            )
        )

        assertThat(documentIds).containsOnly("2", "3", "5")
    }

    @Test
    fun `returns all documents when given no content types to filter by`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1"),
                SearchableVideoMetadataFactory.create(id = "2"),
                SearchableVideoMetadataFactory.create(id = "3", type = VideoType.INSTRUCTIONAL),
                SearchableVideoMetadataFactory.create(id = "4", type = VideoType.NEWS),
                SearchableVideoMetadataFactory.create(id = "5")
            )
        )

        val documentIds = videoIndexReader.search(
            PaginatedSearchRequest(
                VideoQuery(
                    includedType = emptySet()
                ), 0, 10
            )
        )

        assertThat(documentIds).containsOnly("1", "2", "3", "4", "5")
    }

    @Test
    fun `excluded types take precedence`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", type = VideoType.NEWS),
                SearchableVideoMetadataFactory.create(id = "2", type = VideoType.STOCK),
                SearchableVideoMetadataFactory.create(id = "3", type = VideoType.INSTRUCTIONAL),
                SearchableVideoMetadataFactory.create(id = "4", type = VideoType.NEWS),
                SearchableVideoMetadataFactory.create(id = "5", type = VideoType.STOCK)
            )
        )

        val documentIds = videoIndexReader.search(
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

        assertThat(documentIds).containsOnly("3")
    }

    @Test
    fun `excluded types can be combined with phrase queries`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "Wild Rhino", type = VideoType.NEWS),
                SearchableVideoMetadataFactory.create(id = "2", title = "Domesticated Rhino", type = VideoType.NEWS),
                SearchableVideoMetadataFactory.create(id = "3", title = "Cyborg Rhino", type = VideoType.STOCK)
            )
        )

        val documentIds = videoIndexReader.search(
            PaginatedSearchRequest(
                VideoQuery(
                    phrase = "Rhino",
                    excludedType = setOf(
                        VideoType.NEWS
                    )
                ), 0, 10
            )
        )

        assertThat(documentIds).containsOnly("3")
    }
}
