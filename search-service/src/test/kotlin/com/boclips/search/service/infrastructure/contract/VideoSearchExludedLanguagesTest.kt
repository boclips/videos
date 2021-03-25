package com.boclips.search.service.infrastructure.contract

import com.boclips.search.service.domain.common.IndexReader
import com.boclips.search.service.domain.common.IndexWriter
import com.boclips.search.service.domain.common.model.PaginatedIndexSearchRequest
import com.boclips.search.service.domain.videos.model.VideoAccessRuleQuery
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import java.util.Locale

class VideoSearchExludedLanguagesTest : EmbeddedElasticSearchIntegrationTest() {

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `can exclude videos by language`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "hello you", language = Locale.FRENCH),
                SearchableVideoMetadataFactory.create(id = "2", title = "Oh hello again"),
                SearchableVideoMetadataFactory.create(id = "3", title = "hello to you", language = Locale.ITALIAN),
            )
        )

        val results = queryService.search(
            PaginatedIndexSearchRequest(
                query = VideoQuery(
                    phrase = "hello",
                    videoAccessRuleQuery = VideoAccessRuleQuery(excludedLanguages = setOf(Locale.FRENCH))
                )
            )
        )

        assertThat(results.counts.totalHits).isEqualTo(2)
        assertThat(results.elements).containsExactlyInAnyOrder("2", "3")
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `returns videos with any language if not restricted`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "hello you", language = Locale.FRENCH),
                SearchableVideoMetadataFactory.create(id = "2", title = "Oh hello again"),
                SearchableVideoMetadataFactory.create(id = "3", title = "hello to you", language = Locale.ITALIAN),
            )
        )

        val results = queryService.search(
            PaginatedIndexSearchRequest(
                query = VideoQuery(
                    phrase = "hello",
                    videoAccessRuleQuery = VideoAccessRuleQuery()
                )
            )
        )

        assertThat(results.counts.totalHits).isEqualTo(3)
        assertThat(results.elements).containsExactlyInAnyOrder("1", "2", "3")
    }
}