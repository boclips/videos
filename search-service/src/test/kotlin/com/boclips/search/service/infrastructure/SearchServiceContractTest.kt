package com.boclips.search.service.infrastructure

import com.boclips.search.service.domain.SearchService
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
    fun `returns empty collection for empty result`(searchService: SearchService) {
        searchService.createIndex(listOf(SearchableVideoMetadataFactory.create(id = "1", title = "White Gentleman Dancing")))

        val result = searchService.search("query that matches nothing")

        assertThat(result).hasSize(0)
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `finds a video matching metadata`(searchService: SearchService) {

        searchService.createIndex(listOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "White Gentleman Dancing"),
                SearchableVideoMetadataFactory.create(id = "2", title = "Beer", description = "Behave like a gentleman, cane like a sponge"),
                SearchableVideoMetadataFactory.create(id = "3", title = "Mixed-race couple playing piano with a dog", description = "Watch and get educated.")
        ))

        val result = searchService.search("gentleman")

        assertThat(result).containsExactlyInAnyOrder("1", "2")
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `removed videos are not searchable`(searchService: SearchService) {
        searchService.createIndex(listOf(SearchableVideoMetadataFactory.create(id = "1", title = "White Gentleman Dancing")))

        searchService.removeFromSearch("1")

        assertThat(searchService.search("gentleman")).isEmpty()
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `creates a new index and removes the outdated one`(searchService: SearchService) {
        searchService.createIndex(listOf(SearchableVideoMetadataFactory.create(id = "1", title = "Beautiful Boy Dancing")))

        searchService.createIndex(listOf(SearchableVideoMetadataFactory.create(id = "2", title = "Beautiful Girl Dancing")))

        assertThat(searchService.search("beautiful")).isNotEmpty
        assertThat(searchService.search("boy")).isEmpty()
    }

}
