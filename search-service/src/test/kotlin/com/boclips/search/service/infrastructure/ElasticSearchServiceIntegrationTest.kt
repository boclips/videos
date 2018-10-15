package com.boclips.search.service.infrastructure

import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ElasticSearchServiceIntegrationTest : EmbeddedElasticSearchIntegrationTest() {
    @Test
    fun `can deal with mispelled queries`() {
        val searchService = ElasticSearchService(CONFIG)

        searchService.createIndex(listOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "White Gentleman Dancing"),
                SearchableVideoMetadataFactory.create(id = "2", title = "Mixed-race couple playing piano with a dog", description = "Watch and get educated.")
        ))

        val results = searchService.search("gentelman")

        assertThat(results).containsExactly("1")
    }

    @Test
    fun `boosts documents where words appear in sequence in title`() {
        val searchService = ElasticSearchService(CONFIG)

        searchService.createIndex(listOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "Apple banana candy"),
                SearchableVideoMetadataFactory.create(id = "2", title = "candy banana apple"),
                SearchableVideoMetadataFactory.create(id = "3", title = "banana apple candy")
        ))

        val results = searchService.search("Apple banana candy")

        assertThat(results.first()).isEqualTo("1")
    }

    @Test
    fun `boosts documents where words appear in sequence in description`() {
        val searchService = ElasticSearchService(CONFIG)

        searchService.createIndex(listOf(
                SearchableVideoMetadataFactory.create(id = "1", description = "Apple banana candy"),
                SearchableVideoMetadataFactory.create(id = "2", description = "candy banana apple"),
                SearchableVideoMetadataFactory.create(id = "3", description = "banana apple candy")
        ))

        val results = searchService.search("Apple banana candy")

        assertThat(results.first()).isEqualTo("1")
    }

    @Test
    fun `takes stopwords into account for queries like "I have a dream"`() {
        val searchService = ElasticSearchService(CONFIG)

        searchService.createIndex(listOf(
                SearchableVideoMetadataFactory.create(id = "1", description = "dream clouds dream sweet"),
                SearchableVideoMetadataFactory.create(id = "2", description = "i have a dream")
        ))

        val results = searchService.search("i have a dream")

        assertThat(results.first()).isEqualTo("2")
    }

    @Test
    fun `can match word stems eg "it's raining" will match "rain"`() {
        val searchService = ElasticSearchService(CONFIG)

        searchService.createIndex(listOf(
                SearchableVideoMetadataFactory.create(id = "1", description = "it's raining today")
        ))

        val results = searchService.search("rain")

        assertThat(results.first()).isEqualTo("1")
    }

}