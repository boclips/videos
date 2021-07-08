package com.boclips.search.service.infrastructure.contract

import com.boclips.search.service.domain.common.IndexReader
import com.boclips.search.service.domain.common.IndexWriter
import com.boclips.search.service.domain.common.model.CursorBasedIndexSearchRequest
import com.boclips.search.service.domain.videos.model.VideoAccessRuleQuery
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource

class VideoSearchCursorContractTest : EmbeddedElasticSearchIntegrationTest() {
    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `returns a cursor to the next page that can be followed`(
        indexReader: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.safeRebuildIndex(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    title = "Keep Summer safe vol 1"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    title = "Keep Summer safe vol 2"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    title = "Keep Summer safe vol 3"
                ),
            )
        )

        val firstPageResults = indexReader.search(
            CursorBasedIndexSearchRequest(
                query = VideoQuery(videoAccessRuleQuery = VideoAccessRuleQuery()),
                windowSize = 2,
                cursor = null
            )
        )

        val finalPageResults = indexReader.search(
            CursorBasedIndexSearchRequest(
                query = VideoQuery(videoAccessRuleQuery = VideoAccessRuleQuery()),
                windowSize = 2,
                cursor = firstPageResults.cursor
            )
        )

        assertThat(firstPageResults.elements).containsExactly("1", "2")

        assertThat(finalPageResults.elements).containsExactly("3")
    }
}
