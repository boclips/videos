package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.model.PaginatedSearchRequest
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.jupiter.api.BeforeEach

class ESVideoReadSearchServiceQuerySearchesIntegrationTest : EmbeddedElasticSearchIntegrationTest() {

    lateinit var readSearchService: ESVideoReadSearchService
    lateinit var writeSearchService: ESVideoWriteSearchService

    @BeforeEach
    internal fun setUp() {
        readSearchService = ESVideoReadSearchService(EmbeddedElasticSearchIntegrationTest.CONFIG.buildClient())
        writeSearchService = ESVideoWriteSearchService(EmbeddedElasticSearchIntegrationTest.CONFIG.buildClient())
    }

    @Test
    fun `words appear in sequence in title increases rank`() {
        writeSearchService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "Apple banana candy"),
                SearchableVideoMetadataFactory.create(id = "2", title = "candy banana apple"),
                SearchableVideoMetadataFactory.create(id = "3", title = "banana apple candy")
            )
        )

        val results = readSearchService.search(
            PaginatedSearchRequest(query = VideoQuery("Apple banana candy"))
        )

        assertThat(results.first()).isEqualTo("1")
    }

    @Test
    fun `words appear in sequence in description increases rank`() {
        writeSearchService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", description = "Apple banana candy"),
                SearchableVideoMetadataFactory.create(id = "2", description = "candy banana apple"),
                SearchableVideoMetadataFactory.create(id = "3", description = "banana apple candy")
            )
        )

        val results = readSearchService.search(
            PaginatedSearchRequest(query = VideoQuery("Apple banana candy"))
        )

        assertThat(results.first()).isEqualTo("1")
    }

    @Test
    fun `matches keywords`() {
        writeSearchService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "2", keywords = listOf("dog"))
            )
        )

        val results = readSearchService.search(PaginatedSearchRequest(query = VideoQuery("dogs")))

        assertThat(results).containsExactly("2")
    }

    @Test
    fun `content partners must match exactly`() {
        writeSearchService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", contentProvider = "Bozeman Science"),
                SearchableVideoMetadataFactory.create(id = "2", title = "a video about science")
            )
        )

        val results = readSearchService.search(PaginatedSearchRequest(query = VideoQuery(phrase = "science")))

        assertThat(results).containsExactly("2")
    }

    @Test
    fun `transcripts match`() {
        writeSearchService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    transcript = "game of thrones season 8 episode 6 online watch free no ads"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    transcript = "the big bang theory season 8 episode 6 online watch free no ads"
                )
            )
        )

        val results = readSearchService.search(
            PaginatedSearchRequest(query = VideoQuery("thrones"))
        )

        assertThat(results).containsExactly("1")
    }

    @Test
    fun `title match is ranked higher than transcript match`() {
        writeSearchService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    transcript = "game of thrones season 8 episode 6 online watch free no ads"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    title = "game of thrones season 8 is the best one yet"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    transcript = "game of thrones season 8 is the worst one yet"
                )
            )
        )

        val results = readSearchService.search(
            PaginatedSearchRequest(query = VideoQuery("thrones"))
        )

        assertThat(results).hasSize(3)
        assertThat(results).startsWith("2")
    }

    @Test
    fun `takes stopwords into account for queries like "I have a dream"`() {
        writeSearchService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", description = "dream clouds dream sweet"),
                SearchableVideoMetadataFactory.create(id = "2", description = "i have a dream")
            )
        )

        val results = readSearchService.search(
            PaginatedSearchRequest(query = VideoQuery("i have a dream"))
        )

        assertThat(results).containsExactly("2")
    }

    @Test
    fun `can match word stems eg "it's raining" will match "rain"`() {
        writeSearchService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", description = "it's raining today")
            )
        )

        val results = readSearchService.search(
            PaginatedSearchRequest(query = VideoQuery("rain"))
        )

        assertThat(results).containsExactly("1")
    }

    @Test
    fun `exact matches increase rank`() {
        writeSearchService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    title = "Royal Australian Regiment and Operation Dalby - a heli-borne assault during Vietnam War, 16th February, 1967",
                    description = "Royal Australian Regiment and Operation Dalby - a heli-borne assault during Vietnam War, 16th February, 1967. Helicopter fleet, POVs from helicopters."
                ),
                SearchableVideoMetadataFactory.create(id = "2", title = "Napalm bombing during Vietnam War"),
                SearchableVideoMetadataFactory.create(id = "3", title = "bombing during Vietnam War")
            )
        )

        val results = readSearchService.search(
            PaginatedSearchRequest(query = VideoQuery("Napalm bombing during Vietnam War"))
        )

        assertThat(results.first()).isEqualTo("2")
    }

    @Test
    fun `videos match via synonyms`() {
        writeSearchService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", description = "Second world war")
            )
        )

        val results = readSearchService.search(
            PaginatedSearchRequest(query = VideoQuery("WW2"))
        )

        assertThat(results).containsExactly("1")
    }

    @Test
    fun `multiword synonyms must match exactly`() {
        writeSearchService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", description = "video about ww2")
            )
        )

        assertThat(
            readSearchService.search(
                PaginatedSearchRequest(query = VideoQuery("second world war"))
            )
        ).containsExactly("1")
        assertThat(
            readSearchService.search(
                PaginatedSearchRequest(query = VideoQuery("second world"))
            )
        ).isEmpty()
    }

    @Test
    fun `multiword synonyms must match video metadata exactly`() {
        writeSearchService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", description = "second world")
            )
        )

        assertThat(
            readSearchService.search(PaginatedSearchRequest(query = VideoQuery(phrase = "ww2")))
        ).isEmpty()
    }

    @Test
    fun `case sensitive synonyms`() {
        writeSearchService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", description = "Welcome to the US"),
                SearchableVideoMetadataFactory.create(id = "2", description = "Beware of us")
            )
        )

        val results = readSearchService.search(
            PaginatedSearchRequest(query = VideoQuery("United States of America"))
        )

        assertThat(results).containsExactly("1")
    }
}