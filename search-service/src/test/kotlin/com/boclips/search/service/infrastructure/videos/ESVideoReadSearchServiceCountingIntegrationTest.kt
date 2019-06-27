package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.jupiter.api.BeforeEach

class ESVideoReadSearchServiceCountingIntegrationTest : EmbeddedElasticSearchIntegrationTest() {

    lateinit var readSearchService: ESVideoReadSearchService
    lateinit var writeSearchService: ESVideoWriteSearchService

    @BeforeEach
    internal fun setUp() {
        readSearchService = ESVideoReadSearchService(EmbeddedElasticSearchIntegrationTest.CONFIG.buildClient())
        writeSearchService = ESVideoWriteSearchService(EmbeddedElasticSearchIntegrationTest.CONFIG.buildClient())
    }

    @Test
    fun `counts search results for phrase queries`() {
        writeSearchService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", description = "Apple banana candy"),
                SearchableVideoMetadataFactory.create(id = "2", description = "candy banana apple"),
                SearchableVideoMetadataFactory.create(id = "3", description = "candy banana apple"),
                SearchableVideoMetadataFactory.create(id = "4", description = "candy banana apple"),
                SearchableVideoMetadataFactory.create(id = "5", description = "candy banana apple"),
                SearchableVideoMetadataFactory.create(id = "6", description = "candy banana apple"),
                SearchableVideoMetadataFactory.create(id = "7", description = "candy banana apple"),
                SearchableVideoMetadataFactory.create(id = "8", description = "candy banana apple"),
                SearchableVideoMetadataFactory.create(id = "9", description = "candy banana apple"),
                SearchableVideoMetadataFactory.create(id = "10", description = "candy banana apple"),
                SearchableVideoMetadataFactory.create(id = "11", description = "candy banana apple")
            )
        )

        val results = readSearchService.count(VideoQuery(phrase = "banana"))

        assertThat(results).isEqualTo(11)
    }

    @Test
    fun `counts search results for IDs queries`() {
        writeSearchService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "Apple banana candy"),
                SearchableVideoMetadataFactory.create(id = "2", title = "candy banana apple"),
                SearchableVideoMetadataFactory.create(id = "3", title = "banana apple candy")
            )
        )

        val results = readSearchService.count(VideoQuery(ids = listOf("2", "5")))

        assertThat(results).isEqualTo(1)
    }

    @Test
    fun `can count for just news results`() {
        writeSearchService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "3", description = "candy banana apple"),
                SearchableVideoMetadataFactory.create(
                    id = "4",
                    description = "candy banana apple",
                    tags = listOf("news")
                )
            )
        )

        val results = readSearchService.count(VideoQuery(includeTags = listOf("news")))

        assertThat(results).isEqualTo(1)
    }
}