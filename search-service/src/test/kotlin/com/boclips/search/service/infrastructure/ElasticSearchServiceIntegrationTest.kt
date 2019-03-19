package com.boclips.search.service.infrastructure

import com.boclips.search.service.domain.PaginatedSearchRequest
import com.boclips.search.service.domain.Query
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ElasticSearchServiceIntegrationTest : EmbeddedElasticSearchIntegrationTest() {

    lateinit var queryService: ElasticSearchService
    lateinit var adminService: ElasticSearchServiceAdmin

    @BeforeEach
    internal fun setUp() {
        queryService = ElasticSearchService(CONFIG)
        adminService = ElasticSearchServiceAdmin(CONFIG)
    }

    @Test
    fun `calling upsert doesn't delete the index`() {
        adminService.upsert(
                sequenceOf(
                        SearchableVideoMetadataFactory.create(id = "1", title = "Apple banana candy")
                )
        )
        adminService.upsert(
                sequenceOf(
                        SearchableVideoMetadataFactory.create(id = "2", title = "candy banana apple")
                )
        )

        val results = queryService.search(PaginatedSearchRequest(query = Query("candy")))

        assertThat(results).hasSize(2)
    }

    @Test
    fun `document relevance is higher when words appear in sequence in title`() {
        adminService.upsert(
                sequenceOf(
                        SearchableVideoMetadataFactory.create(id = "1", title = "Apple banana candy"),
                        SearchableVideoMetadataFactory.create(id = "2", title = "candy banana apple"),
                        SearchableVideoMetadataFactory.create(id = "3", title = "banana apple candy")
                )
        )

        val results = queryService.search(PaginatedSearchRequest(query = Query("Apple banana candy")))

        assertThat(results.first()).isEqualTo("1")
    }

    @Test
    fun `document relevance is higher when words appear in sequence in description`() {
        adminService.upsert(
                sequenceOf(
                        SearchableVideoMetadataFactory.create(id = "1", description = "Apple banana candy"),
                        SearchableVideoMetadataFactory.create(id = "2", description = "candy banana apple"),
                        SearchableVideoMetadataFactory.create(id = "3", description = "banana apple candy")
                )
        )

        val results = queryService.search(PaginatedSearchRequest(query = Query("Apple banana candy")))

        assertThat(results.first()).isEqualTo("1")
    }

    @Test
    fun `returns documents where there is a keyword match`() {
        adminService.upsert(
                sequenceOf(
                        SearchableVideoMetadataFactory.create(id = "2", keywords = listOf("dog"))
                )
        )

        val results = queryService.search(PaginatedSearchRequest(query = Query("dogs")))

        assertThat(results).containsExactly("2")
    }

    @Test
    fun `returns documents where content partner matches exactly`() {
        adminService.upsert(
                sequenceOf(
                        SearchableVideoMetadataFactory.create(id = "1", contentProvider = "Bozeman Science"),
                        SearchableVideoMetadataFactory.create(id = "2", title = "a video about science")
                )
        )

        val results = queryService.search(PaginatedSearchRequest(query = Query("science")))

        assertThat(results).containsExactly("2")
    }

    @Test
    fun `returns documents where content partner matches exactly, respecting excluded tags`() {
        val contentProvider = "Bozeman Science"

        adminService.upsert(
                sequenceOf(
                        SearchableVideoMetadataFactory.create(id = "1", contentProvider = contentProvider, tags = listOf("news")),
                        SearchableVideoMetadataFactory.create(id = "2", contentProvider = contentProvider, tags = emptyList())
                )
        )

        val results = queryService.search(PaginatedSearchRequest(query = Query(contentProvider, excludeTags = listOf("news"))))

        assertThat(results).containsExactly("2")
    }

    @Test
    fun `returns documents where content partner matches exactly, respecting include tags`() {
        val contentProvider = "Bozeman Science"

        adminService.upsert(
                sequenceOf(
                        SearchableVideoMetadataFactory.create(id = "1", contentProvider = contentProvider, tags = listOf("education")),
                        SearchableVideoMetadataFactory.create(id = "2", contentProvider = contentProvider, tags = emptyList())
                )
        )

        val results = queryService.search(PaginatedSearchRequest(query = Query(contentProvider, includeTags = listOf("education"))))

        assertThat(results).containsExactly("1")
    }

    @Test
    fun `content partner match is ranked higher than matches in other fields`() {
        adminService.upsert(
                sequenceOf(
                        SearchableVideoMetadataFactory.create(id = "1", title = "TED-Ed"),
                        SearchableVideoMetadataFactory.create(id = "2", description = "TED-Ed"),
                        SearchableVideoMetadataFactory.create(id = "3", contentProvider = "TED-Ed"),
                        SearchableVideoMetadataFactory.create(id = "4", keywords = listOf("TED-Ed")),
                        SearchableVideoMetadataFactory.create(id = "5", title = "TED-Ed", description = "TED-Ed", keywords = listOf("TED-Ed"))
                )
        )

        val results = queryService.search(PaginatedSearchRequest(query = Query("Ted-ed")))

        assertThat(results).startsWith("3")
    }

    @Test
    fun `takes stopwords into account for queries like "I have a dream"`() {
        adminService.upsert(
                sequenceOf(
                        SearchableVideoMetadataFactory.create(id = "1", description = "dream clouds dream sweet"),
                        SearchableVideoMetadataFactory.create(id = "2", description = "i have a dream")
                )
        )

        val results = queryService.search(PaginatedSearchRequest(query = Query("i have a dream")))

        assertThat(results).containsExactly("2")
    }

    @Test
    fun `can match word stems eg "it's raining" will match "rain"`() {
        adminService.upsert(
                sequenceOf(
                        SearchableVideoMetadataFactory.create(id = "1", description = "it's raining today")
                )
        )

        val results = queryService.search(PaginatedSearchRequest(query = Query("rain")))

        assertThat(results).containsExactly("1")
    }

    @Test
    fun `exact phrase matches are returned higher than other documents with matching words`() {
        adminService.upsert(
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

        val results = queryService.search(PaginatedSearchRequest(query = Query("Napalm bombing during Vietnam War")))

        assertThat(results.first()).isEqualTo("2")
    }

    @Test
    fun `counts search results for phrase queries`() {
        adminService.upsert(
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

        val results = queryService.count(Query("banana"))

        assertThat(results).isEqualTo(11)
    }

    @Test
    fun `counts search results for IDs queries`() {
        adminService.upsert(
                sequenceOf(
                        SearchableVideoMetadataFactory.create(id = "1", title = "Apple banana candy"),
                        SearchableVideoMetadataFactory.create(id = "2", title = "candy banana apple"),
                        SearchableVideoMetadataFactory.create(id = "3", title = "banana apple candy")
                )
        )

        val results = queryService.count(Query(ids = listOf("2", "5")))

        assertThat(results).isEqualTo(1)
    }

    @Test
    fun `paginates search results`() {
        adminService.upsert(
                sequenceOf(
                        SearchableVideoMetadataFactory.create(id = "1", description = "Apple banana candy"),
                        SearchableVideoMetadataFactory.create(id = "2", description = "candy banana apple"),
                        SearchableVideoMetadataFactory.create(id = "3", description = "candy banana apple"),
                        SearchableVideoMetadataFactory.create(id = "4", description = "candy banana apple")
                )
        )

        val results =
                queryService.search(PaginatedSearchRequest(query = Query("banana"), startIndex = 0, windowSize = 2))

        assertThat(results.size).isEqualTo(2)
    }

    @Test
    fun `can retrieve any page`() {
        adminService.upsert(
                sequenceOf(
                        SearchableVideoMetadataFactory.create(id = "1", description = "Apple banana candy"),
                        SearchableVideoMetadataFactory.create(id = "2", description = "candy banana apple"),
                        SearchableVideoMetadataFactory.create(id = "3", description = "candy banana apple"),
                        SearchableVideoMetadataFactory.create(id = "4", description = "candy banana apple")
                )
        )

        val page1 = queryService.search(PaginatedSearchRequest(query = Query("banana"), startIndex = 0, windowSize = 2))
        val page2 = queryService.search(PaginatedSearchRequest(query = Query("banana"), startIndex = 2, windowSize = 2))
        val page3 = queryService.search(PaginatedSearchRequest(query = Query("banana"), startIndex = 4, windowSize = 2))

        assertThat(page1).doesNotContainAnyElementsOf(page2)
        assertThat(page1).hasSize(2)
        assertThat(page2).hasSize(2)
        assertThat(page3).hasSize(0)
    }

    @Test
    fun `returns exact matches for IDs search query`() {
        adminService.upsert(
                sequenceOf(
                        SearchableVideoMetadataFactory.create(id = "1", title = "Apple banana candy"),
                        SearchableVideoMetadataFactory.create(id = "2", title = "candy banana apple"),
                        SearchableVideoMetadataFactory.create(id = "3", title = "banana apple candy")
                )
        )

        val results = queryService.search(PaginatedSearchRequest(query = Query(ids = listOf("2", "5"))))

        assertThat(results).containsExactly("2")
    }

    @Test
    fun `can retrieve just news`() {
        adminService.upsert(
                sequenceOf(
                        SearchableVideoMetadataFactory.create(id = "3", description = "candy banana apple"),
                        SearchableVideoMetadataFactory.create(
                                id = "4",
                                description = "candy banana apple",
                                tags = listOf("news")
                        )
                )
        )

        val results =
                queryService.search(PaginatedSearchRequest(query = Query(phrase = "banana", includeTags = listOf("news"))))

        assertThat(results).containsExactly("4")
    }

    @Test
    fun `can retrieve news that matches query`() {
        adminService.upsert(
                sequenceOf(
                        SearchableVideoMetadataFactory.create(id = "3", description = "random isNews", tags = listOf("news")),
                        SearchableVideoMetadataFactory.create(
                                id = "4",
                                description = "candy banana apple",
                                tags = listOf("news")
                        )
                )
        )

        val results =
                queryService.search(PaginatedSearchRequest(query = Query(phrase = "banana", includeTags = listOf("news"))))

        assertThat(results).containsExactly("4")
    }

    @Test
    fun `can retrieve non-news that matches query`() {
        adminService.upsert(
                sequenceOf(
                        SearchableVideoMetadataFactory.create(id = "3", description = "some random banana isNews"),
                        SearchableVideoMetadataFactory.create(
                                id = "4",
                                description = "candy banana apple",
                                tags = listOf("news")
                        )
                )
        )

        val results =
                queryService.search(PaginatedSearchRequest(query = Query(phrase = "banana", excludeTags = listOf("news"))))

        assertThat(results).containsExactly("3")
    }

    @Test
    fun `searching with no filters returns news and non-news`() {
        adminService.upsert(
                sequenceOf(
                        SearchableVideoMetadataFactory.create(id = "3", description = "banana"),
                        SearchableVideoMetadataFactory.create(
                                id = "9",
                                description = "candy banana apple",
                                tags = listOf("news")
                        ),
                        SearchableVideoMetadataFactory.create(id = "10", description = "candy banana apple")
                )
        )

        val results = queryService.search(PaginatedSearchRequest(query = Query(phrase = "banana")))

        assertThat(results).hasSize(3)
    }

    @Test
    fun `can retrieve educational videos that matches query`() {
        adminService.upsert(
                sequenceOf(
                        SearchableVideoMetadataFactory.create(id = "3", description = "random isNews"),
                        SearchableVideoMetadataFactory.create(
                                id = "9",
                                description = "candy banana apple",
                                tags = listOf("classroom")
                        ),
                        SearchableVideoMetadataFactory.create(id = "10", description = "candy banana apple")
                )
        )

        val results = queryService.search(
                PaginatedSearchRequest(
                        query = Query(
                                phrase = "banana",
                                includeTags = listOf("classroom")
                        )
                )
        )

        assertThat(results).containsExactly("9")
    }

    @Test
    fun `can count for just news results`() {
        adminService.upsert(
                sequenceOf(
                        SearchableVideoMetadataFactory.create(id = "3", description = "candy banana apple"),
                        SearchableVideoMetadataFactory.create(
                                id = "4",
                                description = "candy banana apple",
                                tags = listOf("news")
                        )
                )
        )

        val results = queryService.count(Query(phrase = "banana", includeTags = listOf("news")))

        assertThat(results).isEqualTo(1)
    }

    @Test
    fun `strictly match the include tags`() {
        adminService.upsert(
                sequenceOf(
                        SearchableVideoMetadataFactory.create(id = "3", description = "banana", tags = listOf("classroom"))
                )
        )

        val results = queryService.search(
                PaginatedSearchRequest(
                        query = Query(
                                phrase = "banana",
                                includeTags = listOf("classroom", "news")
                        )
                )
        )

        assertThat(results).isEmpty()
    }

    @Test
    fun `match any exclude tag`() {
        adminService.upsert(
                sequenceOf(
                        SearchableVideoMetadataFactory.create(id = "3", description = "banana", tags = listOf("classroom"))
                )
        )

        val results = queryService.search(
                PaginatedSearchRequest(
                        query = Query(
                                phrase = "banana",
                                excludeTags = listOf("classroom", "news")
                        )
                )
        )

        assertThat(results).isEmpty()
    }

    @Test
    fun `having include and exclude as the same tag returns no results`() {
        adminService.upsert(
                sequenceOf(
                        SearchableVideoMetadataFactory.create(id = "3", description = "banana", tags = listOf("classroom"))
                )
        )

        val results = queryService.search(
                PaginatedSearchRequest(
                        query = Query(
                                phrase = "banana",
                                excludeTags = listOf("classroom"),
                                includeTags = listOf("classroom")
                        )
                )
        )

        assertThat(results).isEmpty()
    }

    @Test
    fun `videos match via synonyms`() {
        adminService.upsert(
                sequenceOf(
                        SearchableVideoMetadataFactory.create(id = "1", description = "Second world war")
                )
        )

        val results = queryService.search(PaginatedSearchRequest(query = Query("WW2")))

        assertThat(results).containsExactly("1")
    }

    @Test
    fun `multiword synonyms must match query entirely`() {
        adminService.upsert(
                sequenceOf(
                        SearchableVideoMetadataFactory.create(id = "1", description = "video about ww2")
                )
        )

        assertThat(queryService.search(PaginatedSearchRequest(query = Query("second world war")))).containsExactly("1")
        assertThat(queryService.search(PaginatedSearchRequest(query = Query("second world")))).isEmpty()
    }

    @Test
    fun `multiword synonyms must match video metadata entirely`() {
        adminService.upsert(
                sequenceOf(
                        SearchableVideoMetadataFactory.create(id = "1", description = "second world")
                )
        )

        assertThat(queryService.search(PaginatedSearchRequest(query = Query("ww2")))).isEmpty()
    }

    @Test
    fun `case sensitive synonyms`() {
        adminService.upsert(
                sequenceOf(
                        SearchableVideoMetadataFactory.create(id = "1", description = "Welcome to the US"),
                        SearchableVideoMetadataFactory.create(id = "2", description = "Beware of us")
                )
        )

        val results = queryService.search(PaginatedSearchRequest(query = Query("United States of America")))

        assertThat(results).containsExactly("1")
    }
}
