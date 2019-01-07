package com.boclips.search.service.infrastructure

import com.boclips.search.service.domain.*
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import java.util.stream.Stream

class SearchServiceProvider : ArgumentsProvider {
    override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {
        val inMemorySearchService = InMemorySearchService()
        val elasticSearchService = ElasticSearchService(EmbeddedElasticSearchIntegrationTest.CONFIG)

        return Stream.of(
                inMemorySearchService,
                elasticSearchService
        ).map { searchService -> Arguments.of(searchService) }
    }
}

class SearchServiceContractTest : EmbeddedElasticSearchIntegrationTest() {

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `returns empty collection for empty result`(searchService: GenericSearchService<VideoMetadata>) {
        searchService.upsert(sequenceOf(SearchableVideoMetadataFactory.create(id = "1", title = "White Gentleman Dancing")))

        val result = searchService.search(PaginatedSearchRequest(query = Query("query that matches nothing")))

        assertThat(result).hasSize(0)
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `finds a video matching metadata`(searchService: GenericSearchService<VideoMetadata>) {
        searchService.upsert(sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "White Gentleman Dancing"),
                SearchableVideoMetadataFactory.create(id = "2", title = "Beer", description = "Behave like a gentleman, cane like a sponge"),
                SearchableVideoMetadataFactory.create(id = "3", title = "Mixed-race couple playing piano with a dog", description = "Watch and get educated."),
                SearchableVideoMetadataFactory.create(id = "4", title = "Who are you, really?", contentProvider = "Gentleman Ben")
        ))

        val result = searchService.search(PaginatedSearchRequest(query = Query("gentleman")))

        assertThat(result).containsExactlyInAnyOrder("1", "2", "4")
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `finds news videos`(searchService: GenericSearchService<VideoMetadata>) {
        searchService.upsert(sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "May Dancing", typeId = 1),
                SearchableVideoMetadataFactory.create(id = "2", title = "Beer Trump", description = "Behave like a gentleman, cane like a sponge"),
                SearchableVideoMetadataFactory.create(id = "4", title = "Trump to attack UK", contentProvider = "BBC", typeId = 1)
        ))

        val result = searchService.search(PaginatedSearchRequest(query = Query("Trump", filters = listOf(Filter(VideoMetadata::typeId, 1)))))

        assertThat(result).containsExactlyInAnyOrder("4")
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `filters videos`(searchService: GenericSearchService<VideoMetadata>) {
        searchService.upsert(sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "White Gentleman Dancing", contentProvider = "ted"),
                SearchableVideoMetadataFactory.create(id = "2", title = "Beer", contentProvider = "tod"),
                SearchableVideoMetadataFactory.create(id = "3", title = "Not a match", contentProvider = "ted")
        ))

        val result = searchService.search(PaginatedSearchRequest(query = Query("gentleman", filters = listOf(Filter(VideoMetadata::contentProvider, "ted")))))

        assertThat(result).containsExactly("1")
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `paginates results`(searchService: GenericSearchService<VideoMetadata>) {
        searchService.upsert(sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "White Gentleman Dancing"),
                SearchableVideoMetadataFactory.create(id = "2", title = "Beer", description = "Behave like a gentleman, cane like a sponge"),
                SearchableVideoMetadataFactory.create(id = "3", title = "Mixed-race couple playing piano with a dog", description = "Watch and get educated."),
                SearchableVideoMetadataFactory.create(id = "4", title = "Who are you, really?", contentProvider = "Gentleman Ben")
        ))

        val page1 = searchService.search(PaginatedSearchRequest(query = Query("gentleman"), startIndex = 0, windowSize = 2))
        val page2 = searchService.search(PaginatedSearchRequest(query = Query("gentleman"), startIndex = 2, windowSize = 2))

        assertThat(page1).hasSize(2)
        assertThat(page2).hasSize(1)
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `counts all videos matching metadata`(searchService: GenericSearchService<VideoMetadata>) {
        searchService.upsert(sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "White Gentleman Dancing"),
                SearchableVideoMetadataFactory.create(id = "2", title = "Beer", description = "Behave like a gentleman, cane like a sponge"),
                SearchableVideoMetadataFactory.create(id = "3", title = "Mixed-race couple playing piano with a dog", description = "Watch and get educated."),
                SearchableVideoMetadataFactory.create(id = "4", title = "Who are you, really?", contentProvider = "Gentleman Ben")
        ))

        val result = searchService.count(query = Query("gentleman"))

        assertThat(result).isEqualTo(3)
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `removed videos are not searchable`(searchService: GenericSearchService<VideoMetadata>) {
        searchService.upsert(sequenceOf(SearchableVideoMetadataFactory.create(id = "1", title = "White Gentleman Dancing")))

        searchService.removeFromSearch("1")

        assertThat(searchService.search(PaginatedSearchRequest(query = Query("gentleman"))).isEmpty())
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `creates a new index and removes the outdated one`(searchService: GenericSearchService<VideoMetadata>) {
        searchService.upsert(sequenceOf(SearchableVideoMetadataFactory.create(id = "1", title = "Beautiful Boy Dancing")))

        searchService.resetIndex()

        assertThat(searchService.search(PaginatedSearchRequest(query = Query("boy"))).isEmpty())
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `returns existing ids`(searchService: GenericSearchService<VideoMetadata>) {
        searchService.upsert(sequenceOf(SearchableVideoMetadataFactory.create(id = "1", title = "Beautiful Boy Dancing")))

        val query = Query.parse("id:1,2,3,4")
        assertThat(searchService.count(query)).isEqualTo(1)

        val searchResults = searchService.search(PaginatedSearchRequest(query = query, startIndex = 0, windowSize = 2))
        assertThat(searchResults).containsExactly("1")
    }

}
