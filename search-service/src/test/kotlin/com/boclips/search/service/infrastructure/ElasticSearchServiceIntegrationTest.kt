package com.boclips.search.service.infrastructure

import com.boclips.search.service.domain.GenericSearchService
import com.boclips.search.service.domain.PaginatedSearchRequest
import com.boclips.search.service.domain.VideoMetadata
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ElasticSearchServiceIntegrationTest : EmbeddedElasticSearchIntegrationTest() {

    lateinit var searchService: GenericSearchService<VideoMetadata>

    @BeforeEach
    internal fun setUp() {
        searchService = ElasticSearchService(CONFIG)
        searchService.resetIndex()
    }

    @Test
    fun `prefers documents with exact matches over fuzzy matches`() {
        searchService.upsert(sequenceOf(
                SearchableVideoMetadataFactory.create(
                        id = "2",
                        title = "Hugh Dancy & Claire Danes pose on the red carpet",
                        description = "Hugh Dancy & Claire Danes pose on the red carpet"),
                SearchableVideoMetadataFactory.create(
                        id = "1",
                        title = "Dancing:",
                        description = """
                            Will Old Time Dances wipe the floor with Modern Rhythms? says Beryl de Querton. Disclaimer:
                             British Movietone is an historical collection. Any views and expressions within either the
                             video or metadata of the collection are reproduced for historical accuracy and do not
                             represent the opinions or editorial policies of the Associated Press.
                             SHOTLIST: Elevated shot of the dance floor and couples dancing in old time style.
                             """),
                SearchableVideoMetadataFactory.create(
                        id = "3",
                        title = "DANCE FESTIVAL",
                        description = """
                            Disclaimer: British Movietone is an historical collection. Any views and expressions within
                            either the video or metadata of the collection are reproduced for historical accuracy and do
                            not represent the opinions or editorial policies of the Associated Press.
                            SHOTLIST:
                            GV and CU novices dancing. CU winners of novices with award (Mr Hope and Miss Cornwall). SCU
                            formation dancing by Liverpool A Team. CU presentation to winners of formation. CU winners
                            Liverpool A posing.
                         """
                )

        ))

        val results = searchService.search(PaginatedSearchRequest(query = "dance"))

        assertThat(results.size).isEqualTo(3)
        assertThat(results.last()).isEqualTo("2")
    }

    @Test
    fun `can deal with mispelled queries`() {
        searchService.upsert(sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "White Gentleman Dancing"),
                SearchableVideoMetadataFactory.create(id = "2", title = "Mixed-race couple playing piano with a dog", description = "Watch and get educated.")
        ))

        val results = searchService.search(PaginatedSearchRequest(query = "gentelman"))

        assertThat(results).containsExactly("1")
    }

    @Test
    fun `boosts documents where words appear in sequence in title`() {
        searchService.upsert(sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "Apple banana candy"),
                SearchableVideoMetadataFactory.create(id = "2", title = "candy banana apple"),
                SearchableVideoMetadataFactory.create(id = "3", title = "banana apple candy")
        ))

        val results = searchService.search(PaginatedSearchRequest(query = "Apple banana candy"))

        assertThat(results.first()).isEqualTo("1")
    }

    @Test
    fun `boosts documents where words appear in sequence in description`() {
        searchService.upsert(sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", description = "Apple banana candy"),
                SearchableVideoMetadataFactory.create(id = "2", description = "candy banana apple"),
                SearchableVideoMetadataFactory.create(id = "3", description = "banana apple candy")
        ))

        val results = searchService.search(PaginatedSearchRequest(query = "Apple banana candy"))

        assertThat(results.first()).isEqualTo("1")
    }

    @Test
    fun `boosts documents where there is a keyword match`() {
        searchService.upsert(sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", keywords = listOf("cat")),
                SearchableVideoMetadataFactory.create(id = "2", keywords = listOf("dog"))
        ))

        val results = searchService.search(PaginatedSearchRequest(query = "dogs"))

        assertThat(results.first()).isEqualTo("2")
    }

    @Test
    fun `boosts documents where there is a content partner match`() {
        searchService.upsert(sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", contentProvider = "Mr Bean"),
                SearchableVideoMetadataFactory.create(id = "2", contentProvider = "TED Talks")
        ))

        val results = searchService.search(PaginatedSearchRequest(query = "ted talk"))

        assertThat(results.first()).isEqualTo("2")
    }

    @Test
    fun `takes stopwords into account for queries like "I have a dream"`() {
        searchService.upsert(sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", description = "dream clouds dream sweet"),
                SearchableVideoMetadataFactory.create(id = "2", description = "i have a dream")
        ))

        val results = searchService.search(PaginatedSearchRequest(query = "i have a dream"))

        assertThat(results.first()).isEqualTo("2")
    }

    @Test
    fun `can match word stems eg "it's raining" will match "rain"`() {
        searchService.upsert(sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", description = "it's raining today")
        ))

        val results = searchService.search(PaginatedSearchRequest(query = "rain"))

        assertThat(results.first()).isEqualTo("1")
    }

    @Test
    fun `counts search results`() {
        searchService.upsert(sequenceOf(
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
        ))

        val results = searchService.count("banana")

        assertThat(results).isEqualTo(11)
    }

    @Test
    fun `paginates search results`() {
        searchService.upsert(sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", description = "Apple banana candy"),
                SearchableVideoMetadataFactory.create(id = "2", description = "candy banana apple"),
                SearchableVideoMetadataFactory.create(id = "3", description = "candy banana apple"),
                SearchableVideoMetadataFactory.create(id = "4", description = "candy banana apple")
        ))

        val results = searchService.search(PaginatedSearchRequest(query = "banana", startIndex = 0, windowSize = 2))

        assertThat(results.size).isEqualTo(2)
    }

    @Test
    fun `can retrieve any page`() {
        searchService.upsert(sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", description = "Apple banana candy"),
                SearchableVideoMetadataFactory.create(id = "2", description = "candy banana apple"),
                SearchableVideoMetadataFactory.create(id = "3", description = "candy banana apple"),
                SearchableVideoMetadataFactory.create(id = "4", description = "candy banana apple")
        ))

        val page1 = searchService.search(PaginatedSearchRequest(query = "banana", startIndex = 0, windowSize = 2))
        val page2 = searchService.search(PaginatedSearchRequest(query = "banana", startIndex = 2, windowSize = 2))
        val page3 = searchService.search(PaginatedSearchRequest(query = "banana", startIndex = 4, windowSize = 2))

        assertThat(page1).doesNotContainAnyElementsOf(page2)
        assertThat(page1).hasSize(2)
        assertThat(page2).hasSize(2)
        assertThat(page3).hasSize(0)
    }
}