package com.boclips.search.service.infrastructure

import com.boclips.search.service.domain.GenericSearchService
import com.boclips.search.service.domain.GenericSearchServiceAdmin
import com.boclips.search.service.domain.PaginatedSearchRequest
import com.boclips.search.service.domain.Query
import com.boclips.search.service.domain.Sort
import com.boclips.search.service.domain.SortOrder
import com.boclips.search.service.domain.SourceType
import com.boclips.search.service.domain.VideoMetadata
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
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
        val inMemorySearchService = InMemorySearchService()
        val elasticSearchService = ElasticSearchService(EmbeddedElasticSearchIntegrationTest.CONFIG)
        val elasticSearchServiceAdmin = ElasticSearchServiceAdmin(EmbeddedElasticSearchIntegrationTest.CONFIG)

        return Stream.of(
            Arguments.of(inMemorySearchService, inMemorySearchService),
            Arguments.of(elasticSearchService, elasticSearchServiceAdmin)
        )
    }
}

class SearchServiceContractTest : EmbeddedElasticSearchIntegrationTest() {

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `returns empty collection for empty result`(
        queryService: GenericSearchService,
        adminService: GenericSearchServiceAdmin<VideoMetadata>
    ) {
        adminService.safeRebuildIndex(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    title = "White Gentleman Dancing"
                )
            )
        )

        val result = queryService.search(PaginatedSearchRequest(query = Query("query that matches nothing")))

        assertThat(result).hasSize(0)
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `finds a video matching metadata`(
        queryService: GenericSearchService,
        adminService: GenericSearchServiceAdmin<VideoMetadata>
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

        val result = queryService.search(PaginatedSearchRequest(query = Query("gentleman")))

        assertThat(result).containsExactlyInAnyOrder("1", "2", "4")
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `finds news videos`(
        queryService: GenericSearchService,
        adminService: GenericSearchServiceAdmin<VideoMetadata>
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

        val result = queryService.search(PaginatedSearchRequest(query = Query("Trump", includeTags = listOf("news"))))

        assertThat(result).containsExactlyInAnyOrder("4")
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `paginates results`(
        queryService: GenericSearchService,
        adminService: GenericSearchServiceAdmin<VideoMetadata>
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
            queryService.search(PaginatedSearchRequest(query = Query("gentleman"), startIndex = 0, windowSize = 2))
        val page2 =
            queryService.search(PaginatedSearchRequest(query = Query("gentleman"), startIndex = 2, windowSize = 2))

        assertThat(page1).hasSize(2)
        assertThat(page2).hasSize(1)
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `counts all videos matching metadata`(
        queryService: GenericSearchService,
        adminService: GenericSearchServiceAdmin<VideoMetadata>
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

        val result = queryService.count(query = Query("gentleman"))

        assertThat(result).isEqualTo(3)
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `removed videos are not searchable`(
        queryService: GenericSearchService,
        adminService: GenericSearchServiceAdmin<VideoMetadata>
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

        assertThat(queryService.search(PaginatedSearchRequest(query = Query("gentleman"))).isEmpty())
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `creates a new index and removes the outdated one`(
        queryService: GenericSearchService,
        adminService: GenericSearchServiceAdmin<VideoMetadata>
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

        assertThat(queryService.search(PaginatedSearchRequest(query = Query("boy"))).isEmpty())
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `creates a new index and upserts the videos provided`(
        queryService: GenericSearchService,
        adminService: GenericSearchServiceAdmin<VideoMetadata>
    ) {
        adminService.safeRebuildIndex(
            sequenceOf(SearchableVideoMetadataFactory.create(id = "1", title = "Beautiful Boy Dancing"))
        )

        assertThat(queryService.count(query = Query("Boy"))).isEqualTo(1)
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `returns existing ids`(
        queryService: GenericSearchService,
        adminService: GenericSearchServiceAdmin<VideoMetadata>
    ) {
        adminService.safeRebuildIndex(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    title = "Beautiful Boy Dancing"
                )
            )
        )

        val query = Query(ids = listOf("1", "2", "3", "4"))
        assertThat(queryService.count(query)).isEqualTo(1)

        val searchResults = queryService.search(PaginatedSearchRequest(query = query, startIndex = 0, windowSize = 2))
        assertThat(searchResults).containsExactly("1")
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `returns a sorted list by ReleaseDate ascending`(
        queryService: GenericSearchService,
        adminService: GenericSearchServiceAdmin<VideoMetadata>
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
            Query(phrase = "dancing", sort = Sort(fieldName = VideoMetadata::releaseDate, order = SortOrder.ASC))
        assertThat(queryService.count(query)).isEqualTo(3)

        val searchResults = queryService.search(PaginatedSearchRequest(query = query, startIndex = 0, windowSize = 3))
        assertThat(searchResults).containsExactly("yesterday", "today", "tomorrow")
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `returns a sorted list by ReleaseDate descending`(
        queryService: GenericSearchService,
        adminService: GenericSearchServiceAdmin<VideoMetadata>
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
            Query(phrase = "dancing", sort = Sort(fieldName = VideoMetadata::releaseDate, order = SortOrder.DESC))
        assertThat(queryService.count(query)).isEqualTo(3)

        val searchResults = queryService.search(PaginatedSearchRequest(query = query, startIndex = 0, windowSize = 3))
        assertThat(searchResults).containsExactly("tomorrow", "today", "yesterday")
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `can filter by duration bound`(
        queryService: GenericSearchService,
        adminService: GenericSearchServiceAdmin<VideoMetadata>
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
                query = Query(
                    "World war",
                    minDuration = Duration.ofSeconds(5),
                    maxDuration = Duration.ofSeconds(10)
                )
            )
        )

        assertThat(results).containsAll(listOf("1", "2"))
        assertThat(results).doesNotContainAnyElementsOf(listOf("0", "3"))
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `can filter by duration lower bound`(
        queryService: GenericSearchService,
        adminService: GenericSearchServiceAdmin<VideoMetadata>
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
                query = Query(
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
        queryService: GenericSearchService,
        adminService: GenericSearchServiceAdmin<VideoMetadata>
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
            queryService.search(PaginatedSearchRequest(query = Query("World war", maxDuration = Duration.ofSeconds(9))))

        assertThat(results).containsAll(listOf("0", "1"))
        assertThat(results).doesNotContainAnyElementsOf(listOf("2", "3"))
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `can filter by source`(
        queryService: GenericSearchService,
        adminService: GenericSearchServiceAdmin<VideoMetadata>
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
            queryService.search(PaginatedSearchRequest(query = Query("World war", source = SourceType.BOCLIPS)))

        assertThat(results).containsAll(listOf("0", "2"))
        assertThat(results).doesNotContainAnyElementsOf(listOf("1", "3"))
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `can filter by release date range`(
        queryService: GenericSearchService,
        adminService: GenericSearchServiceAdmin<VideoMetadata>
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
                    query = Query(
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
        queryService: GenericSearchService,
        adminService: GenericSearchServiceAdmin<VideoMetadata>
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
                    query = Query(
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
        queryService: GenericSearchService,
        adminService: GenericSearchServiceAdmin<VideoMetadata>
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
                    query = Query(
                        "World war",
                        releaseDateTo = LocalDate.of(2002, 5, 5)
                    )
                )
            )

        assertThat(results).containsAll(listOf("1", "0"))
        assertThat(results).doesNotContainAnyElementsOf(listOf("2", "3"))
    }
}
