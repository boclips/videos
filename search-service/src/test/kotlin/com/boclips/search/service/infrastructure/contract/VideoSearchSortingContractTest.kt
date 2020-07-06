package com.boclips.search.service.infrastructure.contract

import com.boclips.search.service.domain.common.IndexReader
import com.boclips.search.service.domain.common.IndexWriter
import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.domain.common.model.Sort
import com.boclips.search.service.domain.common.model.SortOrder
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
import org.assertj.core.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import java.time.LocalDate

class VideoSearchSortingContractTest : EmbeddedElasticSearchIntegrationTest() {

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `returns a sorted list by ReleaseDate ascending`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.safeRebuildIndex(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "today",
                    title = "Beautiful Boy Dancing",
                    releaseDate = LocalDate.now()
                ),
                SearchableVideoMetadataFactory.create(
                    id = "yesterday",
                    title = "Beautiful Girl Dancing",
                    releaseDate = LocalDate.now().minusDays(1)
                ),
                SearchableVideoMetadataFactory.create(
                    id = "tomorrow",
                    title = "Beautiful Dog Dancing",
                    releaseDate = LocalDate.now().plusDays(1)
                )
            )
        )

        val results = queryService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    phrase = "dancing",
                    videoSort = Sort.ByField(
                        fieldName = VideoMetadata::releaseDate,
                        order = SortOrder.ASC
                    )
                ),
                startIndex = 0,
                windowSize = 3
            )
        )
        Assertions.assertThat(results.elements).containsExactly("yesterday", "today", "tomorrow")
        Assertions.assertThat(results.counts.totalHits).isEqualTo(3)
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `returns a sorted list by ReleaseDate descending`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.safeRebuildIndex(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "today",
                    title = "Beautiful Boy Dancing",
                    releaseDate = LocalDate.now()
                ),
                SearchableVideoMetadataFactory.create(
                    id = "yesterday",
                    title = "Beautiful Girl Dancing",
                    releaseDate = LocalDate.now().minusDays(1)
                ),
                SearchableVideoMetadataFactory.create(
                    id = "tomorrow",
                    title = "Beautiful Dog Dancing",
                    releaseDate = LocalDate.now().plusDays(1)
                )
            )
        )

        val results = queryService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    phrase = "dancing",
                    videoSort = Sort.ByField(
                        fieldName = VideoMetadata::releaseDate,
                        order = SortOrder.DESC
                    )
                ),
                startIndex = 0,
                windowSize = 3
            )
        )
        Assertions.assertThat(results.elements).containsExactly("tomorrow", "today", "yesterday")
        Assertions.assertThat(results.counts.totalHits).isEqualTo(3)
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `returns a list sorted by meanRating`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.safeRebuildIndex(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "lowRating",
                    title = "Beautiful Person Dancing",
                    meanRating = 2.1
                ),
                SearchableVideoMetadataFactory.create(
                    id = "highRating",
                    title = "Beautiful Other Person Dancing",
                    meanRating = 4.2
                ),
                SearchableVideoMetadataFactory.create(
                    id = "middleRating",
                    title = "Beautiful Dog Dancing",
                    meanRating = 3.8
                ), SearchableVideoMetadataFactory.create(
                    id = "noRating",
                    title = "Beautiful Cat Dancing",
                    meanRating = null
                )
            )
        )

        val results = queryService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    phrase = "dancing",
                    videoSort = Sort.ByField(
                        fieldName = VideoMetadata::meanRating,
                        order = SortOrder.DESC
                    )
                ),
                startIndex = 0,
                windowSize = 20
            )
        )

        Assertions.assertThat(results.elements).isEqualTo(listOf("highRating", "middleRating", "lowRating", "noRating"))
        Assertions.assertThat(results.counts.totalHits).isEqualTo(4)
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `returns a list sorted by title ascending`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.safeRebuildIndex(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "bTitle",
                    title = "Beautiful Person Dancing"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "aTitle",
                    title = "An other Beautiful Person Dancing"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "dTitle",
                    title = "Dog dancing"
                ), SearchableVideoMetadataFactory.create(
                    id = "wTitle",
                    title = "Weather report dancing"
                )
            )
        )

        val results = queryService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    phrase = "dancing",
                    videoSort = Sort.ByField(
                        fieldName = VideoMetadata::rawTitle,
                        order = SortOrder.ASC
                    )
                ),
                startIndex = 0,
                windowSize = 20
            )
        )

        Assertions.assertThat(results.elements).isEqualTo(listOf("aTitle", "bTitle", "dTitle", "wTitle"))
        Assertions.assertThat(results.counts.totalHits).isEqualTo(4)
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `returns a randomly sorted list`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.safeRebuildIndex(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "today",
                    title = "Beautiful Boy Dancing",
                    releaseDate = LocalDate.now()
                ),
                SearchableVideoMetadataFactory.create(
                    id = "yesterday",
                    title = "Beautiful Girl Dancing",
                    releaseDate = LocalDate.now().minusDays(1)
                ),
                SearchableVideoMetadataFactory.create(
                    id = "tomorrow",
                    title = "Beautiful Dog Dancing",
                    releaseDate = LocalDate.now().plusDays(1)
                )
            )
        )

        val results = queryService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    videoSort = Sort.ByRandom()
                ),
                startIndex = 0,
                windowSize = 3
            )
        )

        Assertions.assertThat(results.elements).containsExactlyInAnyOrder("tomorrow", "today", "yesterday")
        Assertions.assertThat(results.counts.totalHits).isEqualTo(3)
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `sorting randomly doesn't impact query matching`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.safeRebuildIndex(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "White Gentleman Dancing"),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    title = "Mixed-race couple playing piano with a dog",
                    description = "Watch and get educated."
                )
            )
        )

        val result = queryService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    phrase = "gentleman",
                    videoSort = Sort.ByRandom()
                )
            )
        )

        Assertions.assertThat(result.elements).containsExactlyInAnyOrder("1")
        Assertions.assertThat(result.counts.totalHits).isEqualTo(1)
    }
}
