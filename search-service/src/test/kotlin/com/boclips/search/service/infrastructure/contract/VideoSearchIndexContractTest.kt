package com.boclips.search.service.infrastructure.contract

import com.boclips.search.service.domain.common.IndexReader
import com.boclips.search.service.domain.common.IndexWriter
import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
import org.assertj.core.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource

class VideoSearchIndexContractTest : EmbeddedElasticSearchIntegrationTest() {
    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `removed videos are not searchable`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.safeRebuildIndex(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    title = "White Gentleman Dancing"
                )
            )
        )

        adminService.removeFromSearch("1")

        Assertions.assertThat(
            queryService.search(
                PaginatedSearchRequest(
                    query = VideoQuery(
                        "gentleman"
                    )
                )
            ).elements.isEmpty()
        )
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `can bulk remove videos from index`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.safeRebuildIndex(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    title = "White Gentleman Dancing"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    title = "White Gentleman Dancing"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    title = "White Gentleman Dancing"
                )
            )
        )

        adminService.bulkRemoveFromSearch(listOf("1", "2", "3"))

        Assertions.assertThat(
            queryService.search(
                PaginatedSearchRequest(
                    query = VideoQuery(
                        "gentleman"
                    )
                )
            ).elements.isEmpty()
        )
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `creates a new index and removes the outdated one`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.safeRebuildIndex(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    title = "Beautiful Boy Dancing"
                )
            )
        )

        adminService.safeRebuildIndex(emptySequence())

        Assertions.assertThat(
            queryService.search(
                PaginatedSearchRequest(
                    query = VideoQuery(
                        "boy"
                    )
                )
            ).elements.isEmpty()
        )
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `creates a new index and upserts the videos provided`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.safeRebuildIndex(
            sequenceOf(SearchableVideoMetadataFactory.create(id = "1", title = "Beautiful Boy Dancing"))
        )

        Assertions.assertThat(queryService.search(PaginatedSearchRequest(query = VideoQuery("Boy"))).counts.totalHits)
            .isEqualTo(1)
    }
}
