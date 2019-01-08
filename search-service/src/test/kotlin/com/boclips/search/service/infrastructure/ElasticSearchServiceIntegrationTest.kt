package com.boclips.search.service.infrastructure

import com.boclips.search.service.domain.*
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

        val results = searchService.search(PaginatedSearchRequest(query = Query("dance")))

        assertThat(results.size).isEqualTo(3)
        assertThat(results.last()).isEqualTo("2")
    }

    @Test
    fun `can deal with mispelled queries`() {
        searchService.upsert(sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "White Gentleman Dancing"),
                SearchableVideoMetadataFactory.create(id = "2", title = "Mixed-race couple playing piano with a dog", description = "Watch and get educated.")
        ))

        val results = searchService.search(PaginatedSearchRequest(query = Query("gentelman")))

        assertThat(results).containsExactly("1")
    }

    @Test
    fun `document relevance is higher when words appear in sequence in title`() {
        searchService.upsert(sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "Apple banana candy"),
                SearchableVideoMetadataFactory.create(id = "2", title = "candy banana apple"),
                SearchableVideoMetadataFactory.create(id = "3", title = "banana apple candy")
        ))

        val results = searchService.search(PaginatedSearchRequest(query = Query("Apple banana candy")))

        assertThat(results.first()).isEqualTo("1")
    }

    @Test
    fun `document relevance is higher when words appear in sequence in description`() {
        searchService.upsert(sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", description = "Apple banana candy"),
                SearchableVideoMetadataFactory.create(id = "2", description = "candy banana apple"),
                SearchableVideoMetadataFactory.create(id = "3", description = "banana apple candy")
        ))

        val results = searchService.search(PaginatedSearchRequest(query = Query("Apple banana candy")))

        assertThat(results.first()).isEqualTo("1")
    }

    @Test
    fun `boosts documents where there is a keyword match`() {
        searchService.upsert(sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", keywords = listOf("cat")),
                SearchableVideoMetadataFactory.create(id = "2", keywords = listOf("dog"))
        ))

        val results = searchService.search(PaginatedSearchRequest(query = Query("dogs")))

        assertThat(results).containsExactly("2")
    }

    @Test
    fun `boosts documents where there is a content partner match`() {
        searchService.upsert(sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", contentProvider = "Mr Bean"),
                SearchableVideoMetadataFactory.create(id = "2", contentProvider = "TED Talks")
        ))

        val results = searchService.search(PaginatedSearchRequest(query = Query("ted talk")))

        assertThat(results).containsExactly("2")
    }

    @Test
    fun `takes stopwords into account for queries like "I have a dream"`() {
        searchService.upsert(sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", description = "dream clouds dream sweet"),
                SearchableVideoMetadataFactory.create(id = "2", description = "i have a dream")
        ))

        val results = searchService.search(PaginatedSearchRequest(query = Query("i have a dream")))

        assertThat(results).containsExactly("2")
    }

    @Test
    fun `can match word stems eg "it's raining" will match "rain"`() {
        searchService.upsert(sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", description = "it's raining today")
        ))

        val results = searchService.search(PaginatedSearchRequest(query = Query("rain")))

        assertThat(results).containsExactly("1")
    }

    @Test
    fun `counts search results for phrase queries`() {
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

        val results = searchService.count(Query("banana"))

        assertThat(results).isEqualTo(11)
    }

    @Test
    fun `counts search results for IDs queries`() {
        searchService.upsert(sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "Apple banana candy"),
                SearchableVideoMetadataFactory.create(id = "2", title = "candy banana apple"),
                SearchableVideoMetadataFactory.create(id = "3", title = "banana apple candy")
        ))

        val results = searchService.count(Query(ids = listOf("2", "5")))

        assertThat(results).isEqualTo(1)
    }

    @Test
    fun `paginates search results`() {
        searchService.upsert(sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", description = "Apple banana candy"),
                SearchableVideoMetadataFactory.create(id = "2", description = "candy banana apple"),
                SearchableVideoMetadataFactory.create(id = "3", description = "candy banana apple"),
                SearchableVideoMetadataFactory.create(id = "4", description = "candy banana apple")
        ))

        val results = searchService.search(PaginatedSearchRequest(query = Query("banana"), startIndex = 0, windowSize = 2))

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

        val page1 = searchService.search(PaginatedSearchRequest(query = Query("banana"), startIndex = 0, windowSize = 2))
        val page2 = searchService.search(PaginatedSearchRequest(query = Query("banana"), startIndex = 2, windowSize = 2))
        val page3 = searchService.search(PaginatedSearchRequest(query = Query("banana"), startIndex = 4, windowSize = 2))

        assertThat(page1).doesNotContainAnyElementsOf(page2)
        assertThat(page1).hasSize(2)
        assertThat(page2).hasSize(2)
        assertThat(page3).hasSize(0)
    }

    @Test
    fun `returns exact matches for IDs search query`() {
        searchService.upsert(sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "Apple banana candy"),
                SearchableVideoMetadataFactory.create(id = "2", title = "candy banana apple"),
                SearchableVideoMetadataFactory.create(id = "3", title = "banana apple candy")
        ))

        val results = searchService.search(PaginatedSearchRequest(query = Query(ids = listOf("2", "5"))))

        assertThat(results).containsExactly("2")
    }

    @Test
    fun `can retrieve just news`() {
        searchService.upsert(sequenceOf(
                SearchableVideoMetadataFactory.create(id = "3", description = "candy banana apple", isNews = false),
                SearchableVideoMetadataFactory.create(id = "4", description = "candy banana apple", isNews = true)
        ))

        val results = searchService.search(PaginatedSearchRequest(query = Query(phrase = "banana", filters = listOf(Filter(VideoMetadata::isNews, true)))))

        assertThat(results).containsExactly("4")
    }

    @Test
    fun `can retrieve news that matches query`() {
        searchService.upsert(sequenceOf(
                SearchableVideoMetadataFactory.create(id = "3", description = "random isNews", isNews = true),
                SearchableVideoMetadataFactory.create(id = "4", description = "candy banana apple", isNews = true)
        ))

        val results = searchService.search(PaginatedSearchRequest(query = Query(phrase = "banana", filters = listOf(Filter(VideoMetadata::isNews, true)))))

        assertThat(results).containsExactly("4")
    }

    @Test
    fun `can retrieve non-news that matches query`() {
        searchService.upsert(sequenceOf(
                SearchableVideoMetadataFactory.create(id = "3", description = "some random banana isNews"),
                SearchableVideoMetadataFactory.create(id = "4", description = "candy banana apple", isNews = true)
        ))

        val results = searchService.search(PaginatedSearchRequest(query = Query(phrase = "banana", filters = listOf(Filter(VideoMetadata::isNews, false)))))

        assertThat(results).containsExactly("3")
    }

    @Test
    fun `searching with no filters returns news and non-news`() {
        searchService.upsert(sequenceOf(
                SearchableVideoMetadataFactory.create(id = "3", description = "banana", isNews = false),
                SearchableVideoMetadataFactory.create(id = "9", description = "candy banana apple", isNews = true),
                SearchableVideoMetadataFactory.create(id = "10", description = "candy banana apple", isNews = false)
        ))

        val results = searchService.search(PaginatedSearchRequest(query = Query(phrase = "banana", filters = emptyList())))

        assertThat(results).hasSize(3)
    }

    @Test
    fun `can retrieve educational videos that matches query`() {
        searchService.upsert(sequenceOf(
                SearchableVideoMetadataFactory.create(id = "3", description = "random isNews", isEducational = false),
                SearchableVideoMetadataFactory.create(id = "9", description = "candy banana apple", isEducational = true),
                SearchableVideoMetadataFactory.create(id = "10", description = "candy banana apple", isEducational = false)
        ))

        val results = searchService.search(PaginatedSearchRequest(query = Query(phrase = "banana", filters = listOf(Filter(VideoMetadata::isEducational, true)))))

        assertThat(results).containsExactly("9")
    }

    @Test
    fun `can count for just news results`() {
        searchService.upsert(sequenceOf(
                SearchableVideoMetadataFactory.create(id = "3", description = "candy banana apple"),
                SearchableVideoMetadataFactory.create(id = "4", description = "candy banana apple", isNews = true)
        ))

        val results = searchService.count(Query(phrase = "banana", filters = listOf(Filter(VideoMetadata::isNews, true))))

        assertThat(results).isEqualTo(1)
    }
}
