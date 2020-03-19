package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.ReindexPropertiesFactory
import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class VideoIndexReaderContentPartnerFilterIntegrationTest : EmbeddedElasticSearchIntegrationTest() {
    private lateinit var videoIndexReader: VideoIndexReader
    private lateinit var videoIndexWriter: VideoIndexWriter

    @BeforeEach
    fun setUp() {
        videoIndexReader = VideoIndexReader(esClient)
        videoIndexWriter = VideoIndexWriter.createTestInstance(esClient, ReindexPropertiesFactory.create())
    }

    @Test
    fun `single-content-partner filter`() {
        contentPartnerTest(
            given = sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    contentProvider = "provider one"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    contentProvider = "provider two"
                )
            ),
            searchFor = VideoQuery(
                contentPartnerNames = setOf("provider two")
            ),
            expectIds = listOf("2")
        )
    }

    @Test
    fun `multi-content-partner filter`() {
        contentPartnerTest(
            given = sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    contentProvider = "p1"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    contentProvider = "p1"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    contentProvider = "p2"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "4",
                    contentProvider = "p2"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "5",
                    contentProvider = "p3"
                )
            ),
            searchFor = VideoQuery(
                contentPartnerNames = setOf("p2", "p3")
            ),
            expectIds = listOf("3", "4", "5")
        )
    }

    @Test
    fun `content-partner occurs before query string matching`() {
        contentPartnerTest(
            given = sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    title = "math",
                    contentProvider = "p1"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    title = "science",
                    contentProvider = "p1"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    title = "math",
                    contentProvider = "p2"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "4",
                    title = "science",
                    contentProvider = "p2"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "5",
                    title = "science",
                    contentProvider = "p3"
                )
            ),
            searchFor = VideoQuery(
                contentPartnerNames = setOf("p1"),
                phrase = "science"
            ),
            expectIds = listOf("2")
        )
    }

    private fun contentPartnerTest(given: Sequence<VideoMetadata>, searchFor: VideoQuery, expectIds: List<String>) {
        videoIndexWriter.upsert(given)

        val results = videoIndexReader.search(
            PaginatedSearchRequest(query = searchFor)
        )

        assertThat(results).containsExactlyInAnyOrder(
            *expectIds.toTypedArray()
        )
    }

    @Nested
    inner class ExcludedContentPartnerIds {
        @Test
        fun `excludes content partner even when filtering by a content partner`() {
            contentPartnerTest(
                given = sequenceOf(
                    SearchableVideoMetadataFactory.create(
                        id = "1",
                        title = "math",
                        contentProvider = "p1",
                        contentPartnerId = "cp-id-1"
                    ),
                    SearchableVideoMetadataFactory.create(
                        id = "2",
                        title = "science",
                        contentProvider = "p1",
                        contentPartnerId = "cp-id-1"
                    ),
                    SearchableVideoMetadataFactory.create(
                        id = "3",
                        title = "science",
                        contentProvider = "p2",
                        contentPartnerId = "cp-id-2"
                    )
                ),
                searchFor = VideoQuery(
                    contentPartnerNames = setOf("p1", "p2"),
                    excludedContentPartnerIds = setOf("cp-id-1")
                ),
                expectIds = listOf("3")
            )
        }

        @Test
        fun `excludes multiple content partners`() {
            contentPartnerTest(
                given = sequenceOf(
                    SearchableVideoMetadataFactory.create(
                        id = "1",
                        title = "math",
                        contentPartnerId = "cp-id-1"
                    ),
                    SearchableVideoMetadataFactory.create(
                        id = "2",
                        title = "science",
                        contentPartnerId = "cp-id-2"
                    ),
                    SearchableVideoMetadataFactory.create(
                        id = "3",
                        title = "science",
                        contentPartnerId = "cp-id-3"
                    )
                ),
                searchFor = VideoQuery(
                    phrase = "science",
                    excludedContentPartnerIds = setOf("cp-id-1", "cp-id-3")
                ),
                expectIds = listOf("2")
            )
        }
    }
}
