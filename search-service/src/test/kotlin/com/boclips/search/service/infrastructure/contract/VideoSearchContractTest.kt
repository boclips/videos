package com.boclips.search.service.infrastructure.contract

import com.boclips.search.service.domain.common.IndexReader
import com.boclips.search.service.domain.common.IndexWriter
import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.domain.common.model.Sort
import com.boclips.search.service.domain.common.model.SortOrder
import com.boclips.search.service.domain.videos.model.SourceType
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.infrastructure.videos.VideoIndexReader
import com.boclips.search.service.infrastructure.videos.VideoIndexWriter
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.TestFactories.createSubjectMetadata
import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
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
        val elasticSearchServiceAdmin = VideoIndexWriter.createTestInstance(EmbeddedElasticSearchIntegrationTest.CLIENT.buildClient())

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

        assertThat(result).hasSize(0)
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

        assertThat(result).containsExactlyInAnyOrder("1", "2", "4")
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `finds news videos`(
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
                    includeTags = listOf("news")
                )
            )
        )

        assertThat(result).containsExactlyInAnyOrder("4")
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

        val results = queryService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    "video"
                )
            )
        )

        assertThat(results).containsExactly("1")
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

        assertThat(page1).hasSize(2)
        assertThat(page2).hasSize(1)
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `counts all videos matching metadata`(
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

        val result = queryService.count(VideoQuery("gentleman"))

        assertThat(result).isEqualTo(3)
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
            ).isEmpty()
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
            ).isEmpty()
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
            ).isEmpty()
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

        assertThat(queryService.count(VideoQuery("Boy"))).isEqualTo(1)
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
        assertThat(queryService.count(query)).isEqualTo(1)

        val searchResults = queryService.search(
            PaginatedSearchRequest(
                query = query,
                startIndex = 0,
                windowSize = 2
            )
        )
        assertThat(searchResults).containsExactly("1")
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

        val query =
            VideoQuery(
                phrase = "dancing",
                sort = Sort(
                    fieldName = VideoMetadata::releaseDate,
                    order = SortOrder.ASC
                )
            )
        assertThat(queryService.count(query)).isEqualTo(3)

        val searchResults = queryService.search(
            PaginatedSearchRequest(
                query = query,
                startIndex = 0,
                windowSize = 3
            )
        )
        assertThat(searchResults).containsExactly("yesterday", "today", "tomorrow")
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

        val query =
            VideoQuery(
                phrase = "dancing",
                sort = Sort(
                    fieldName = VideoMetadata::releaseDate,
                    order = SortOrder.DESC
                )
            )
        assertThat(queryService.count(query)).isEqualTo(3)

        val searchResults = queryService.search(
            PaginatedSearchRequest(
                query = query,
                startIndex = 0,
                windowSize = 3
            )
        )
        assertThat(searchResults).containsExactly("tomorrow", "today", "yesterday")
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
                    minDuration = Duration.ofSeconds(10)
                )
            )
        )

        assertThat(results).containsAll(listOf("2", "3"))
        assertThat(results).doesNotContainAnyElementsOf(listOf("0", "1"))
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
                        maxDuration = Duration.ofSeconds(9)
                    )
                )
            )

        assertThat(results).containsAll(listOf("0", "1"))
        assertThat(results).doesNotContainAnyElementsOf(listOf("2", "3"))
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

        assertThat(results).containsAll(listOf("0", "2"))
        assertThat(results).doesNotContainAnyElementsOf(listOf("1", "3"))
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

        assertThat(results).containsAll(listOf("0", "1"))
        assertThat(results).doesNotContainAnyElementsOf(listOf("2", "3"))
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

        assertThat(results).containsAll(listOf("2", "3"))
        assertThat(results).doesNotContainAnyElementsOf(listOf("1", "0"))
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

        assertThat(results).containsAll(listOf("1", "0"))
        assertThat(results).doesNotContainAnyElementsOf(listOf("2", "3"))
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `can filter by age range when min and max is within bounds`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "0",
                    description = "Zeroth world war",
                    ageRangeMin = 5,
                    ageRangeMax = 7
                ),
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    description = "First world war",
                    ageRangeMin = 7,
                    ageRangeMax = 9
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    description = "Second world war",
                    ageRangeMin = 7,
                    ageRangeMax = 11
                ),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    description = "Third world war",
                    ageRangeMin = 3,
                    ageRangeMax = 4
                ),
                SearchableVideoMetadataFactory.create(
                    id = "4",
                    description = "Fourth world war",
                    ageRangeMin = 15,
                    ageRangeMax = 18
                ),
                SearchableVideoMetadataFactory.create(
                    id = "5",
                    description = "Fifth world war",
                    ageRangeMin = null,
                    ageRangeMax = null
                )
            )
        )

        val results =
            queryService.search(
                PaginatedSearchRequest(
                    query = VideoQuery(
                        "World war",
                        ageRangeMin = 5,
                        ageRangeMax = 11
                    )
                )
            )

        assertThat(results).containsAll(listOf("0", "1", "2"))
        assertThat(results).doesNotContainAnyElementsOf(listOf("4", "3", "5"))
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `can filter by age range when min and max extends outside bounds`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "0",
                    description = "Zeroth world war",
                    ageRangeMin = 3,
                    ageRangeMax = 18
                ),
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    description = "First world war",
                    ageRangeMin = 3,
                    ageRangeMax = 7
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    description = "Second world war",
                    ageRangeMin = 7,
                    ageRangeMax = 15
                ),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    description = "Third world war",
                    ageRangeMin = 3,
                    ageRangeMax = 5
                ),
                SearchableVideoMetadataFactory.create(
                    id = "4",
                    description = "Fifth world war",
                    ageRangeMin = 3,
                    ageRangeMax = 4
                ),
                SearchableVideoMetadataFactory.create(
                    id = "5",
                    description = "Sixth world war",
                    ageRangeMin = 15,
                    ageRangeMax = 18
                ),
                SearchableVideoMetadataFactory.create(
                    id = "6",
                    description = "Seventh world war",
                    ageRangeMin = 7
                )
            )
        )

        val results =
            queryService.search(
                PaginatedSearchRequest(
                    query = VideoQuery(
                        "World war",
                        ageRangeMin = 5,
                        ageRangeMax = 11
                    )
                )
            )

        assertThat(results).containsAll(listOf("0", "1", "2", "3", "6"))
        assertThat(results).doesNotContainAnyElementsOf(listOf("4", "5"))
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `can filter by age range when query has no max`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "0",
                    description = "Zeroth world war",
                    ageRangeMin = 3,
                    ageRangeMax = 18
                ),
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    description = "First world war",
                    ageRangeMin = 3,
                    ageRangeMax = 7
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    description = "Second world war",
                    ageRangeMin = 7,
                    ageRangeMax = 15
                ),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    description = "Third world war",
                    ageRangeMin = 3,
                    ageRangeMax = 5
                ),
                SearchableVideoMetadataFactory.create(
                    id = "4",
                    description = "Fourth world war",
                    ageRangeMin = 7,
                    ageRangeMax = 11
                ),
                SearchableVideoMetadataFactory.create(
                    id = "5",
                    description = "Fifth world war",
                    ageRangeMin = 3,
                    ageRangeMax = 4
                ),
                SearchableVideoMetadataFactory.create(
                    id = "6",
                    description = "Sixth world war",
                    ageRangeMin = 15,
                    ageRangeMax = 18
                ),
                SearchableVideoMetadataFactory.create(
                    id = "7",
                    description = "Seventh world war",
                    ageRangeMin = 7
                )
            )
        )

        val results =
            queryService.search(
                PaginatedSearchRequest(
                    query = VideoQuery(
                        "World war",
                        ageRangeMin = 5
                    )
                )
            )

        assertThat(results).containsAll(listOf("0", "1", "2", "3", "4", "6", "7"))
        assertThat(results).doesNotContainAnyElementsOf(listOf("5"))
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `can filter by age range when query has no min`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "0",
                    description = "Zeroth world war",
                    ageRangeMin = 3,
                    ageRangeMax = 18
                ),
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    description = "First world war",
                    ageRangeMin = 3,
                    ageRangeMax = 7
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    description = "Third world war",
                    ageRangeMin = 5
                ),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    description = "Fourth world war",
                    ageRangeMin = 15,
                    ageRangeMax = 18
                ),
                SearchableVideoMetadataFactory.create(
                    id = "4",
                    description = "Fifth world war",
                    ageRangeMin = 13
                ),
                SearchableVideoMetadataFactory.create(
                    id = "5",
                    description = "Sixth world war",
                    ageRangeMin = null,
                    ageRangeMax = null
                )
            )
        )

        val results =
            queryService.search(
                PaginatedSearchRequest(
                    query = VideoQuery(
                        "World war",
                        ageRangeMax = 11
                    )
                )
            )

        assertThat(results).containsAll(listOf("0", "1", "2"))
        assertThat(results).doesNotContainAnyElementsOf(listOf("3", "4", "5"))
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

        assertThat(results).containsExactly("0")
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
                    subjects = setOf(createSubjectMetadata(id="subject-one"), createSubjectMetadata(id = "subject-two"))
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

        assertThat(results).containsAll(listOf("1", "2"))
        assertThat(results).doesNotContainAnyElementsOf(listOf("0"))
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

        assertThat(results).containsAll(listOf("0", "1", "2"))
        assertThat(results).doesNotContainAnyElementsOf(listOf("3"))
    }
}
