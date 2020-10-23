package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.domain.videos.model.VideoAccessRuleQuery
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class VideoIndexReaderQuotedPhrasesTest : EmbeddedElasticSearchIntegrationTest() {
    private lateinit var videoIndexReader: VideoIndexReader
    private lateinit var videoIndexWriter: VideoIndexWriter

    @BeforeEach
    fun setUp() {
        videoIndexReader = VideoIndexReader(esClient)
        videoIndexWriter = VideoIndexWriter.createTestInstance(esClient, 20)
    }

    @Test
    fun `video containing exact phrase in title are matched, but partial matches are excluded`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    title = "blue"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    title = "cat"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    title = "a blue cat meows"
                )
            )
        )

        val results = videoIndexReader.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    videoAccessRuleQuery = VideoAccessRuleQuery(),
                    phrase = "\"blue cat\""
                )
            )
        )

        assertThat(results.counts.totalHits).isEqualTo(1)
        assertThat(results.elements).containsExactly("3")
    }

    @Test
    fun `video containing exact phrase in description are matched, but partial matches are excluded`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    description = "the red cars drive quickly"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    description = "red cars drive"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    description = "drive"
                )
            )
        )

        val results = videoIndexReader.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    videoAccessRuleQuery = VideoAccessRuleQuery(),
                    phrase = "\"the red cars drive\""
                )
            )
        )

        assertThat(results.counts.totalHits).isEqualTo(1)
        assertThat(results.elements).containsExactly("1")
    }

    @Test
    fun `video containing exact phrase in transcript are matched, but partial matches are excluded`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    transcript = "blue whales eat krill"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    transcript = "blue whales blue whales"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    transcript = "whales"
                )
            )
        )

        val results = videoIndexReader.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    videoAccessRuleQuery = VideoAccessRuleQuery(),
                    phrase = "\"blue whales\""
                )
            )
        )

        assertThat(results.counts.totalHits).isEqualTo(2)
        assertThat(results.elements).containsExactly("2", "1")
    }

    @Test
    fun `multiple quoted phrases require all quoted phrases to match`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    title = "gandalf the grey"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    title = "radagast the brown"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    title = "gandalf the grey sauron",
                    description = "radagast the brown"
                )
            )
        )

        val results = videoIndexReader.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    videoAccessRuleQuery = VideoAccessRuleQuery(),
                    phrase = "\"gandalf the grey\" \"radagast the brown\" \"sauron\""
                )
            )
        )

        assertThat(results.counts.totalHits).isEqualTo(1)
        assertThat(results.elements).containsExactly("3")
    }

    @Test
    fun `relevance of whole input is still considered after filtering for quoted phrases`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    title = "pecan pie blue"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    title = "pecan pie blue berry"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    title = "pecan pie blue berry basket"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "4",
                    title = "pie, pecan blue berry basket"
                )
            )
        )

        val results = videoIndexReader.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    videoAccessRuleQuery = VideoAccessRuleQuery(),
                    phrase = "\"pecan pie\" blue berry basket"
                )
            )
        )

        assertThat(results.counts.totalHits).isEqualTo(3)
        assertThat(results.elements).containsExactly("3", "2", "1")
    }
}
