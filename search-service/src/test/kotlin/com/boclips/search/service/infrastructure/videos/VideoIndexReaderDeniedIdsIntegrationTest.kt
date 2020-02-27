package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class VideoIndexReaderDeniedIdsIntegrationTest : EmbeddedElasticSearchIntegrationTest() {
    private lateinit var videoIndexReader: VideoIndexReader
    private lateinit var videoIndexWriter: VideoIndexWriter

    @BeforeEach
    fun setUp() {
        videoIndexReader = VideoIndexReader(esClient)
        videoIndexWriter = VideoIndexWriter.createTestInstance(esClient)
    }

    @Test
    fun `denied videos are omitted by search`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "apple"),
                SearchableVideoMetadataFactory.create(id = "2", title = "apple & bananas")
            )
        )

        val results = videoIndexReader.search(
            PaginatedSearchRequest(query = VideoQuery(deniedVideoIds = setOf("1"), phrase = "apple"))
        )

        assertThat(results).containsExactly("2")
    }

    @Test
    fun `denied videos are omitted by search, and override permitted ids`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "apple"),
                SearchableVideoMetadataFactory.create(id = "2", title = "apple & bananas")
            )
        )

        val results = videoIndexReader.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    deniedVideoIds = setOf("1"),
                    permittedVideoIds = setOf("1"),
                    phrase = "apple"
                )
            )
        )

        assertThat(results).containsExactly("2")
    }

    @Test
    fun `denied videos not matching any ids are ignored`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "apple"),
                SearchableVideoMetadataFactory.create(id = "2", title = "apple & bananas")
            )
        )

        val results = videoIndexReader.search(
            PaginatedSearchRequest(query = VideoQuery(deniedVideoIds = setOf("100"), phrase = "apple"))
        )

        assertThat(results).containsExactly("1", "2")
    }
}
