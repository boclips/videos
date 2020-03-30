package com.boclips.search.service.infrastructure.contract

import com.boclips.search.service.domain.common.IndexReader
import com.boclips.search.service.domain.common.IndexWriter
import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.domain.common.model.Sort
import com.boclips.search.service.domain.common.model.SortOrder
import com.boclips.search.service.domain.videos.model.AgeRange
import com.boclips.search.service.domain.videos.model.DurationRange
import com.boclips.search.service.domain.videos.model.SourceType
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.domain.videos.model.VideoType
import com.boclips.search.service.infrastructure.videos.VideoIndexReader
import com.boclips.search.service.infrastructure.videos.VideoIndexWriter
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest

import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
import com.boclips.search.service.testsupport.TestFactories.createSubjectMetadata
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import java.time.Duration
import java.time.LocalDate
import java.util.stream.Stream

class SearchServiceProvider : ArgumentsProvider {
    override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {
        val inMemorySearchService = VideoSearchServiceFake()
        val elasticSearchService = VideoIndexReader(EmbeddedElasticSearchIntegrationTest.CLIENT.buildClient())
        val elasticSearchServiceAdmin =
            VideoIndexWriter.createTestInstance(
                EmbeddedElasticSearchIntegrationTest.CLIENT.buildClient(),
                100
            )

        return Stream.of(
            Arguments.of(inMemorySearchService, inMemorySearchService),
            Arguments.of(elasticSearchService, elasticSearchServiceAdmin)
        )
    }
}

class VideoSearchServiceContractTest : EmbeddedElasticSearchIntegrationTest() {
    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `returns empty collection for empty result`(
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

        val result = queryService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    "videoQuery that matches nothing"
                )
            )
        )

        assertThat(result.elements).hasSize(0)
        assertThat(result.counts.totalHits).isEqualTo(0)
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `finds a video matching metadata`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.safeRebuildIndex(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "White Gentleman Dancing"),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    title = "Beer",
                    description = "Behave like a gentleman, cane like a sponge"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    title = "Mixed-race couple playing piano with a dog",
                    description = "Watch and get educated."
                ),
                SearchableVideoMetadataFactory.create(
                    id = "4",
                    title = "Who are you, really?",
                    contentProvider = "Gentleman"
                )
            )
        )

        val result = queryService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    "gentleman"
                )
            )
        )

        assertThat(result.elements).containsExactlyInAnyOrder("1", "2", "4")
        assertThat(result.counts.totalHits).isEqualTo(3)
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
                    sort = Sort.ByRandom()
                )
            )
        )

        assertThat(result.elements).containsExactlyInAnyOrder("1")
        assertThat(result.counts.totalHits).isEqualTo(1)
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `finds videos by best for tags`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.safeRebuildIndex(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "May Dancing", tags = listOf("news")),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    title = "Beer Trump",
                    description = "Behave like a gentleman, cane like a sponge"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "4",
                    title = "Trump to attack UK",
                    contentProvider = "BBC",
                    tags = listOf("news")
                )
            )
        )

        val result = queryService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    "Trump",
                    bestFor = listOf("news")
                )
            )
        )

        assertThat(result.elements).containsExactlyInAnyOrder("4")
        assertThat(result.counts.totalHits).isEqualTo(1)
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `returns all results if best for tags not provided`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.safeRebuildIndex(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "May Dancing", tags = listOf("news")),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    title = "Beer Trump",
                    description = "Behave like a gentleman, cane like a sponge"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "4",
                    title = "Trump to attack UK",
                    contentProvider = "BBC",
                    tags = listOf("news")
                )
            )
        )

        val result = queryService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    "Trump"
                )
            )
        )

        assertThat(result.elements).containsExactlyInAnyOrder("2", "4")
        assertThat(result.counts.totalHits).isEqualTo(2)
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `returns all results if empty best for tags are given`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.safeRebuildIndex(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "May Dancing", tags = listOf("news")),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    title = "Beer Trump",
                    description = "Behave like a gentleman, cane like a sponge"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "4",
                    title = "Trump to attack UK",
                    contentProvider = "BBC",
                    tags = listOf("news")
                )
            )
        )

        val result = queryService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    phrase = "Trump",
                    bestFor = emptyList()
                )
            )
        )

        assertThat(result.elements).containsExactlyInAnyOrder("2", "4")
        assertThat(result.counts.totalHits).isEqualTo(2)
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `finds by video type`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.safeRebuildIndex(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "May Dancing", type = VideoType.NEWS),
                SearchableVideoMetadataFactory.create(id = "2", title = "Trump Dancing", type = VideoType.STOCK),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    title = "Johnson Dancing",
                    type = VideoType.INSTRUCTIONAL
                )
            )
        )

        val result = queryService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    includedType = setOf(VideoType.NEWS, VideoType.STOCK)
                )
            )
        )

        assertThat(result.elements).containsOnly("1", "2")
        assertThat(result.counts.totalHits).isEqualTo(2)
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `filters out videos of excluded video types`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.safeRebuildIndex(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "May Dancing", type = VideoType.NEWS),
                SearchableVideoMetadataFactory.create(id = "2", title = "Trump Dancing", type = VideoType.STOCK),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    title = "Johnson Dancing",
                    type = VideoType.INSTRUCTIONAL
                )
            )
        )

        val result = queryService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    excludedType = setOf(VideoType.NEWS, VideoType.STOCK)
                )
            )
        )

        assertThat(result.elements).containsOnly("3")
        assertThat(result.counts.totalHits).isEqualTo(1)
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `filters out videos with excluded content partner ids`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.safeRebuildIndex(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    title = "May Dancing",
                    contentPartnerId = "excluded-cp"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    title = "Trump Dancing",
                    contentPartnerId = "included-cp"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    title = "Johnson Dancing",
                    contentPartnerId = "included-cp"
                )
            )
        )

        val result = queryService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    excludedContentPartnerIds = setOf("excluded-cp")
                )
            )
        )

        assertThat(result.elements).containsExactlyInAnyOrder("2", "3")
        assertThat(result.counts.totalHits).isEqualTo(2)
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `filters out specified video types when retrieving videos by ids`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.safeRebuildIndex(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", type = VideoType.NEWS),
                SearchableVideoMetadataFactory.create(id = "2", type = VideoType.STOCK),
                SearchableVideoMetadataFactory.create(id = "3", type = VideoType.INSTRUCTIONAL)
            )
        )

        val result = queryService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    ids = listOf("1", "2", "3"),
                    excludedType = setOf(VideoType.NEWS, VideoType.STOCK)
                )
            )
        )

        assertThat(result.elements).containsOnly("3")
        assertThat(result.counts.totalHits).isEqualTo(1)
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `returns all videos when type is not specified`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.safeRebuildIndex(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "May Dancing", type = VideoType.NEWS),
                SearchableVideoMetadataFactory.create(id = "2", title = "Trump Dancing", type = VideoType.STOCK),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    title = "Johnson Dancing",
                    type = VideoType.INSTRUCTIONAL
                )
            )
        )

        val result = queryService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    includedType = emptySet()
                )
            )
        )

        assertThat(result.elements).containsOnly("1", "2", "3")
        assertThat(result.counts.totalHits).isEqualTo(3)
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `searches in transcripts`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.safeRebuildIndex(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", transcript = "the video transcript")
            )
        )

        val result = queryService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    "video"
                )
            )
        )

        assertThat(result.elements).containsExactly("1")
        assertThat(result.counts.totalHits).isEqualTo(1)
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `paginates results`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.safeRebuildIndex(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "White Gentleman Dancing"),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    title = "Beer",
                    description = "Behave like a gentleman, cane like a sponge"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    title = "Mixed-race couple playing piano with a dog",
                    description = "Watch and get educated."
                ),
                SearchableVideoMetadataFactory.create(
                    id = "4",
                    title = "Who are you, really?",
                    contentProvider = "Gentleman"
                )
            )
        )

        val page1 =
            queryService.search(
                PaginatedSearchRequest(
                    query = VideoQuery(
                        "gentleman"
                    ), startIndex = 0, windowSize = 2
                )
            )
        val page2 =
            queryService.search(
                PaginatedSearchRequest(
                    query = VideoQuery(
                        "gentleman"
                    ), startIndex = 2, windowSize = 2
                )
            )

        assertThat(page1.elements).hasSize(2)
        assertThat(page1.counts.totalHits).isEqualTo(3)

        assertThat(page2.elements).hasSize(1)
        assertThat(page2.counts.totalHits).isEqualTo(3)
    }

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

        assertThat(
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

        assertThat(
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

        assertThat(
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

        assertThat(queryService.search(PaginatedSearchRequest(query = VideoQuery("Boy"))).counts.totalHits).isEqualTo(1)
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `returns existing ids`(
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

        val query = VideoQuery(ids = listOf("1", "2", "3", "4"))
        assertThat(queryService.search(PaginatedSearchRequest(query = query)).counts.totalHits).isEqualTo(1)

        val results = queryService.search(
            PaginatedSearchRequest(
                query = query,
                startIndex = 0,
                windowSize = 2
            )
        )
        assertThat(results.elements).containsExactly("1")
        assertThat(results.counts.totalHits).isEqualTo(1)
    }

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
                    sort = Sort.ByField(
                        fieldName = VideoMetadata::releaseDate,
                        order = SortOrder.ASC
                    )
                ),
                startIndex = 0,
                windowSize = 3
            )
        )
        assertThat(results.elements).containsExactly("yesterday", "today", "tomorrow")
        assertThat(results.counts.totalHits).isEqualTo(3)
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
                    sort = Sort.ByField(
                        fieldName = VideoMetadata::releaseDate,
                        order = SortOrder.DESC
                    )
                ),
                startIndex = 0,
                windowSize = 3
            )
        )
        assertThat(results.elements).containsExactly("tomorrow", "today", "yesterday")
        assertThat(results.counts.totalHits).isEqualTo(3)
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
                    sort = Sort.ByField(
                        fieldName = VideoMetadata::meanRating,
                        order = SortOrder.DESC
                    )
                ),
                startIndex = 0,
                windowSize = 20
            )
        )

        assertThat(results.elements).isEqualTo(listOf("highRating", "middleRating", "lowRating", "noRating"))
        assertThat(results.counts.totalHits).isEqualTo(4)
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
                    sort = Sort.ByRandom()
                ),
                startIndex = 0,
                windowSize = 3
            )
        )

        assertThat(results.elements).containsExactlyInAnyOrder("tomorrow", "today", "yesterday")
        assertThat(results.counts.totalHits).isEqualTo(3)
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `can filter by duration lower bound`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "0", description = "Zeroth world war", durationSeconds = 1),
                SearchableVideoMetadataFactory.create(id = "1", description = "First world war", durationSeconds = 5),
                SearchableVideoMetadataFactory.create(id = "2", description = "Second world war", durationSeconds = 10),
                SearchableVideoMetadataFactory.create(id = "3", description = "Third world war", durationSeconds = 15)
            )
        )

        val results = queryService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    "World war",
                    durationRanges = listOf(DurationRange(min = Duration.ofSeconds(10)))
                )
            )
        )

        assertThat(results.elements).containsAll(listOf("2", "3"))
        assertThat(results.elements).doesNotContainAnyElementsOf(listOf("0", "1"))
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `can filter by duration upper bound`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "0", description = "Zeroth world war", durationSeconds = 1),
                SearchableVideoMetadataFactory.create(id = "1", description = "First world war", durationSeconds = 5),
                SearchableVideoMetadataFactory.create(id = "2", description = "Second world war", durationSeconds = 10),
                SearchableVideoMetadataFactory.create(id = "3", description = "Third world war", durationSeconds = 15)
            )
        )

        val results =
            queryService.search(
                PaginatedSearchRequest(
                    query = VideoQuery(
                        "World war",
                        durationRanges = listOf(DurationRange(min = Duration.ofSeconds(0), max = Duration.ofSeconds(9)))
                    )
                )
            )

        assertThat(results.elements).containsAll(listOf("0", "1"))
        assertThat(results.elements).doesNotContainAnyElementsOf(listOf("2", "3"))
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `can filter by source`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "0",
                    description = "Zeroth world war",
                    source = SourceType.BOCLIPS
                ),
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    description = "First world war",
                    source = SourceType.YOUTUBE
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    description = "Second world war",
                    source = SourceType.BOCLIPS
                ),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    description = "Third world war",
                    source = SourceType.YOUTUBE
                )
            )
        )

        val results =
            queryService.search(
                PaginatedSearchRequest(
                    query = VideoQuery(
                        "World war",
                        source = SourceType.BOCLIPS
                    )
                )
            )

        assertThat(results.elements).containsAll(listOf("0", "2"))
        assertThat(results.elements).doesNotContainAnyElementsOf(listOf("1", "3"))
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `can filter by release date range`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "0",
                    description = "Zeroth world war",
                    releaseDate = LocalDate.of(2000, 1, 10)
                ),
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    description = "First world war",
                    releaseDate = LocalDate.of(2002, 1, 1)
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    description = "Second world war",
                    releaseDate = LocalDate.of(2003, 1, 1)
                ),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    description = "Third world war",
                    releaseDate = LocalDate.of(2004, 1, 1)
                )
            )
        )

        val results =
            queryService.search(
                PaginatedSearchRequest(
                    query = VideoQuery(
                        "World war",
                        releaseDateFrom = LocalDate.of(1999, 1, 10),
                        releaseDateTo = LocalDate.of(2002, 1, 10)
                    )
                )
            )

        assertThat(results.elements).containsAll(listOf("0", "1"))
        assertThat(results.elements).doesNotContainAnyElementsOf(listOf("2", "3"))
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `can filter by release date lower bound`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "0",
                    description = "Zeroth world war",
                    releaseDate = LocalDate.of(2000, 1, 10)
                ),
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    description = "First world war",
                    releaseDate = LocalDate.of(2002, 1, 1)
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    description = "Second world war",
                    releaseDate = LocalDate.of(2003, 1, 1)
                ),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    description = "Third world war",
                    releaseDate = LocalDate.of(2004, 1, 1)
                )
            )
        )

        val results =
            queryService.search(
                PaginatedSearchRequest(
                    query = VideoQuery(
                        "World war",
                        releaseDateFrom = LocalDate.of(2002, 5, 5)
                    )
                )
            )

        assertThat(results.elements).containsAll(listOf("2", "3"))
        assertThat(results.elements).doesNotContainAnyElementsOf(listOf("1", "0"))
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `can filter by release date upper bound`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "0",
                    description = "Zeroth world war",
                    releaseDate = LocalDate.of(2000, 1, 10)
                ),
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    description = "First world war",
                    releaseDate = LocalDate.of(2002, 1, 1)
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    description = "Second world war",
                    releaseDate = LocalDate.of(2003, 1, 1)
                ),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    description = "Third world war",
                    releaseDate = LocalDate.of(2004, 1, 1)
                )
            )
        )

        val results =
            queryService.search(
                PaginatedSearchRequest(
                    query = VideoQuery(
                        "World war",
                        releaseDateTo = LocalDate.of(2002, 5, 5)
                    )
                )
            )

        assertThat(results.elements).containsAll(listOf("1", "0"))
        assertThat(results.elements).doesNotContainAnyElementsOf(listOf("2", "3"))
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `can filter by subject`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "0",
                    title = "TED",
                    subjects = setOf(createSubjectMetadata("subject-one"))
                ),
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    title = "TED",
                    subjects = setOf(createSubjectMetadata("subject-two"))
                )
            )
        )

        val results = queryService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    phrase = "TED",
                    subjectIds = setOf("subject-one")
                )
            )
        )

        assertThat(results.elements).containsExactly("0")
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `can filter by promoted`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "0",
                    title = "TED",
                    promoted = false
                ),
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    title = "TED",
                    promoted = true
                )
            )
        )

        val results = queryService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    phrase = "",
                    promoted = true
                )
            )
        )

        assertThat(results.elements).containsExactly("1")
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `can filter by manually-tagged subjects`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "0",
                    title = "TED",
                    subjectsSetManually = true
                ),
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    title = "TED",
                    subjectsSetManually = false
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    title = "TED",
                    subjectsSetManually = null
                )
            )
        )

        val results = queryService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    phrase = "TED",
                    subjectsSetManually = true
                )
            )
        )

        assertThat(results.elements).containsAll(listOf("0"))
        assertThat(results.elements).doesNotContainAnyElementsOf(listOf("1", "2"))
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `can filter by non-manually-tagged subjects`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "0",
                    title = "TED",
                    subjectsSetManually = true
                ),
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    title = "TED",
                    subjectsSetManually = false
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    title = "TED",
                    subjectsSetManually = null
                )
            )
        )

        val results = queryService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    phrase = "TED",
                    subjectsSetManually = false
                )
            )
        )

        assertThat(results.elements).containsAll(listOf("1"))
        assertThat(results.elements).doesNotContainAnyElementsOf(listOf("0", "2"))
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `can filter by subject on videos with multiple subjects`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "0",
                    title = "TED",
                    subjects = setOf(
                        createSubjectMetadata(id = "subject-one"),
                        createSubjectMetadata(id = "subject-two")
                    )
                ),
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    title = "TED",
                    subjects = setOf(createSubjectMetadata("subject-three"))
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    title = "TED",
                    subjects = setOf(createSubjectMetadata("subject-two"), createSubjectMetadata("subject-three"))
                )
            )
        )

        val results = queryService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    phrase = "TED",
                    subjectIds = setOf("subject-three")
                )
            )
        )

        assertThat(results.elements).containsAll(listOf("1", "2"))
        assertThat(results.elements).doesNotContainAnyElementsOf(listOf("0"))
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `can filter with multiple subjects in a query`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "0",
                    title = "TED",
                    subjects = setOf(createSubjectMetadata("subject-one"))
                ),
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    title = "TED",
                    subjects = setOf(createSubjectMetadata("subject-two"))
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    title = "TED",
                    subjects = setOf(createSubjectMetadata("subject-two"), createSubjectMetadata("subject-three"))
                ),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    title = "TED",
                    subjects = setOf(createSubjectMetadata("subject-four"), createSubjectMetadata("subject-three"))
                )
            )
        )

        val results = queryService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    phrase = "TED",
                    subjectIds = setOf("subject-one", "subject-two")
                )
            )
        )

        assertThat(results.elements).containsAll(listOf("0", "1", "2"))
        assertThat(results.elements).doesNotContainAnyElementsOf(listOf("3"))
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `can limit search by permitted ids`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "hello you"),
                SearchableVideoMetadataFactory.create(id = "2", title = "Oh hai"),
                SearchableVideoMetadataFactory.create(id = "3", title = "hello to you")
            )
        )

        val results = queryService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    phrase = "hello",
                    permittedVideoIds = setOf("1", "2")
                )
            )
        )

        assertThat(results.elements).containsExactly("1")
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `does not limit search when permitted ids is null`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "hello you"),
                SearchableVideoMetadataFactory.create(id = "2", title = "Oh hai"),
                SearchableVideoMetadataFactory.create(id = "3", title = "hello to you")
            )
        )

        val results = queryService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    phrase = "hello",
                    permittedVideoIds = null
                )
            )
        )

        assertThat(results.elements).containsExactlyInAnyOrder("1", "3")
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `does not limit search when permitted ids is empty`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "hello you"),
                SearchableVideoMetadataFactory.create(id = "2", title = "Oh hai"),
                SearchableVideoMetadataFactory.create(id = "3", title = "hello to you")
            )
        )

        val results = queryService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    phrase = "hello",
                    permittedVideoIds = emptySet()
                )
            )
        )

        assertThat(results.elements).containsExactlyInAnyOrder("1", "3")
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `can limit search when looking up by id`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1"),
                SearchableVideoMetadataFactory.create(id = "2"),
                SearchableVideoMetadataFactory.create(id = "3")
            )
        )

        val results = queryService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    ids = listOf("1", "2"),
                    permittedVideoIds = setOf("1", "3")
                )
            )
        )

        assertThat(results.elements).containsExactlyInAnyOrder("1")
        assertThat(results.counts.totalHits).isEqualTo(1)
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `does not limit search when looking up by id if no permitted ids are specified`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1"),
                SearchableVideoMetadataFactory.create(id = "2"),
                SearchableVideoMetadataFactory.create(id = "3")
            )
        )

        val results = queryService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    ids = listOf("1", "2"),
                    permittedVideoIds = emptySet()
                )
            )
        )

        assertThat(results.elements).containsExactlyInAnyOrder("1", "2")
        assertThat(results.counts.totalHits).isEqualTo(2)
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `does not limit search when looking up by id if no permitted ids is null`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1"),
                SearchableVideoMetadataFactory.create(id = "2"),
                SearchableVideoMetadataFactory.create(id = "3")
            )
        )

        val results = queryService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    ids = listOf("1", "2"),
                    permittedVideoIds = null
                )
            )
        )

        assertThat(results.elements).containsExactlyInAnyOrder("1", "2")
        assertThat(results.counts.totalHits).isEqualTo(2)
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `does not include denied video ids in id lookup`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1"),
                SearchableVideoMetadataFactory.create(id = "2"),
                SearchableVideoMetadataFactory.create(id = "3")
            )
        )

        val results = queryService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    ids = listOf("1", "2", "3"),
                    deniedVideoIds = setOf("1")
                )
            )
        )

        assertThat(results.elements).containsExactlyInAnyOrder("3", "2")
        assertThat(results.counts.totalHits).isEqualTo(2)
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `does not include denied video ids in search query`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "hello you"),
                SearchableVideoMetadataFactory.create(id = "2", title = "Oh hello"),
                SearchableVideoMetadataFactory.create(id = "3", title = "hello to you")
            )
        )

        val results = queryService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    phrase = "hello",
                    deniedVideoIds = setOf("1", "2")
                )
            )
        )

        assertThat(results.elements).containsExactly("3")
        assertThat(results.counts.totalHits).isEqualTo(1)
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `filters by age range`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", ageRangeMin = 3, ageRangeMax = 7),
                SearchableVideoMetadataFactory.create(id = "2", ageRangeMin = 9, ageRangeMax = 11)
            )
        )

        val results = queryService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    ageRanges = listOf(AgeRange(3, 7))
                )
            )
        )

        assertThat(results.elements).containsExactly("1")
        assertThat(results.counts.totalHits).isEqualTo(1)
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `filters by stream eligibility`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", eligibleForStream = true),
                SearchableVideoMetadataFactory.create(id = "2", eligibleForStream = false)
            )
        )

        val results = queryService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    isEligibleForStream = true
                )
            )
        )

        assertThat(results.elements).containsExactly("1")
        assertThat(results.counts.totalHits).isEqualTo(1)
    }
}
