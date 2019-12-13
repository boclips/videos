package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.domain.videos.model.VideoType
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class VideoIndexReaderPermittedIdsIntegrationTest : EmbeddedElasticSearchIntegrationTest() {
    private lateinit var videoIndexReader: VideoIndexReader
    private lateinit var videoIndexWriter: VideoIndexWriter

    @BeforeEach
    fun setUp() {
        videoIndexReader = VideoIndexReader(esClient)
        videoIndexWriter = VideoIndexWriter.createTestInstance(esClient)
    }

    @Test
    fun `limits search when permitted ids are specified`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "apple"),
                SearchableVideoMetadataFactory.create(id = "2", title = "apple & bananas")
            )
        )

        val results = videoIndexReader.search(
            PaginatedSearchRequest(query = VideoQuery(permittedVideoIds = setOf("1"), phrase = "apple"))
        )

        assertThat(results).containsExactly("1")
    }

    @Test
    fun `other filters still work when limiting search`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "apple", type = VideoType.NEWS),
                SearchableVideoMetadataFactory.create(id = "2", title = "apple & bananas"),
                SearchableVideoMetadataFactory.create(id = "3", title = "golf is cool", type = VideoType.NEWS)
            )
        )

        val results = videoIndexReader.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    permittedVideoIds = setOf("1", "2"),
                    phrase = "apple",
                    type = setOf(VideoType.NEWS)
                )
            )
        )

        assertThat(results).containsExactly("1")
    }

    @Test
    fun `does not limit search when permitted ids is null`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "apple"),
                SearchableVideoMetadataFactory.create(id = "2", title = "apple & bananas")
            )
        )

        val results = videoIndexReader.search(
            PaginatedSearchRequest(query = VideoQuery(permittedVideoIds = null, phrase = "apple"))
        )

        assertThat(results).containsExactlyInAnyOrder("1", "2")
    }

    @Test
    fun `does not limit search when permitted ids is empty`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "apple"),
                SearchableVideoMetadataFactory.create(id = "2", title = "apple & bananas")
            )
        )

        val results = videoIndexReader.search(
            PaginatedSearchRequest(query = VideoQuery(permittedVideoIds = emptySet(), phrase = "apple"))
        )

        assertThat(results).containsExactlyInAnyOrder("1", "2")
    }
}