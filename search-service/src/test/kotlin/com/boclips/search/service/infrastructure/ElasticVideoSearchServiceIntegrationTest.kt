package com.boclips.search.service.infrastructure

import com.boclips.search.service.domain.PaginatedSearchRequest
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.infrastructure.videos.ElasticSearchVideoServiceAdmin
import com.boclips.search.service.infrastructure.videos.ElasticVideoSearchService
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ElasticVideoSearchServiceIntegrationTest : EmbeddedElasticSearchIntegrationTest() {

    lateinit var queryServiceVideo: ElasticVideoSearchService
    lateinit var adminVideoService: ElasticSearchVideoServiceAdmin

    @BeforeEach
    internal fun setUp() {
        queryServiceVideo = ElasticVideoSearchService(CONFIG)
        adminVideoService = ElasticSearchVideoServiceAdmin(CONFIG)
    }

    @Test
    fun `calling upsert doesn't delete the index`() {
        adminVideoService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "Apple banana candy")
            )
        )
        adminVideoService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "2", title = "candy banana apple")
            )
        )

        val results = queryServiceVideo.search(PaginatedSearchRequest(query = VideoQuery(
            "candy"
        )
        ))

        assertThat(results).hasSize(2)
    }

    @Test
    fun `document relevance is higher when words appear in sequence in title`() {
        adminVideoService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "Apple banana candy"),
                SearchableVideoMetadataFactory.create(id = "2", title = "candy banana apple"),
                SearchableVideoMetadataFactory.create(id = "3", title = "banana apple candy")
            )
        )

        val results = queryServiceVideo.search(PaginatedSearchRequest(query = VideoQuery(
            "Apple banana candy"
        )
        ))

        assertThat(results.first()).isEqualTo("1")
    }

    @Test
    fun `document relevance is higher when words appear in sequence in description`() {
        adminVideoService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", description = "Apple banana candy"),
                SearchableVideoMetadataFactory.create(id = "2", description = "candy banana apple"),
                SearchableVideoMetadataFactory.create(id = "3", description = "banana apple candy")
            )
        )

        val results = queryServiceVideo.search(PaginatedSearchRequest(query = VideoQuery(
            "Apple banana candy"
        )
        ))

        assertThat(results.first()).isEqualTo("1")
    }

    @Test
    fun `returns documents where there is a keyword match`() {
        adminVideoService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "2", keywords = listOf("dog"))
            )
        )

        val results = queryServiceVideo.search(PaginatedSearchRequest(query = VideoQuery(
            "dogs"
        )
        ))

        assertThat(results).containsExactly("2")
    }

    @Test
    fun `returns documents where content partner matches exactly`() {
        adminVideoService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", contentProvider = "Bozeman Science"),
                SearchableVideoMetadataFactory.create(id = "2", title = "a video about science")
            )
        )

        val results = queryServiceVideo.search(PaginatedSearchRequest(query = VideoQuery(
            "science"
        )
        ))

        assertThat(results).containsExactly("2")
    }

    @Test
    fun `returns documents where content partner matches exactly, respecting excluded tags`() {
        val contentProvider = "Bozeman Science"

        adminVideoService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    contentProvider = contentProvider,
                    tags = listOf("news")
                ),
                SearchableVideoMetadataFactory.create(id = "2", contentProvider = contentProvider, tags = emptyList())
            )
        )

        val results =
            queryServiceVideo.search(PaginatedSearchRequest(query = VideoQuery(
                contentProvider,
                excludeTags = listOf("news")
            )
            ))

        assertThat(results).containsExactly("2")
    }

    @Test
    fun `returns documents where content partner matches exactly, respecting include tags`() {
        val contentProvider = "Bozeman Science"

        adminVideoService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    contentProvider = contentProvider,
                    tags = listOf("education")
                ),
                SearchableVideoMetadataFactory.create(id = "2", contentProvider = contentProvider, tags = emptyList())
            )
        )

        val results = queryServiceVideo.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    contentProvider,
                    includeTags = listOf("education")
                )
            )
        )

        assertThat(results).containsExactly("1")
    }

    @Test
    fun `content partner match is ranked higher than matches in other fields`() {
        adminVideoService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "TED-Ed"),
                SearchableVideoMetadataFactory.create(id = "2", description = "TED-Ed"),
                SearchableVideoMetadataFactory.create(id = "3", contentProvider = "TED-Ed"),
                SearchableVideoMetadataFactory.create(id = "4", keywords = listOf("TED-Ed")),
                SearchableVideoMetadataFactory.create(
                    id = "5",
                    title = "TED-Ed",
                    description = "TED-Ed",
                    keywords = listOf("TED-Ed")
                )
            )
        )

        val results = queryServiceVideo.search(PaginatedSearchRequest(query = VideoQuery(
            "Ted-ed"
        )
        ))

        assertThat(results).startsWith("3")
    }

    @Test
    fun `returns documents where transcript matches`() {
        adminVideoService.upsert(
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

        val results = queryServiceVideo.search(PaginatedSearchRequest(query = VideoQuery(
            "thrones"
        )
        ))

        assertThat(results).containsExactly("1")
    }

    @Test
    fun `title match is ranked higher than transcript match`() {
        adminVideoService.upsert(
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

        val results = queryServiceVideo.search(PaginatedSearchRequest(query = VideoQuery(
            "thrones"
        )
        ))

        assertThat(results).hasSize(3)
        assertThat(results).startsWith("2")
    }

    @Test
    fun `takes stopwords into account for queries like "I have a dream"`() {
        adminVideoService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", description = "dream clouds dream sweet"),
                SearchableVideoMetadataFactory.create(id = "2", description = "i have a dream")
            )
        )

        val results = queryServiceVideo.search(PaginatedSearchRequest(query = VideoQuery(
            "i have a dream"
        )
        ))

        assertThat(results).containsExactly("2")
    }

    @Test
    fun `can match word stems eg "it's raining" will match "rain"`() {
        adminVideoService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", description = "it's raining today")
            )
        )

        val results = queryServiceVideo.search(PaginatedSearchRequest(query = VideoQuery(
            "rain"
        )
        ))

        assertThat(results).containsExactly("1")
    }

    @Test
    fun `exact phrase matches are returned higher than other documents with matching words`() {
        adminVideoService.upsert(
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

        val results = queryServiceVideo.search(PaginatedSearchRequest(query = VideoQuery(
            "Napalm bombing during Vietnam War"
        )
        ))

        assertThat(results.first()).isEqualTo("2")
    }

    @Test
    fun `counts search results for phrase queries`() {
        adminVideoService.upsert(
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

        val results = queryServiceVideo.count(VideoQuery("banana"))

        assertThat(results).isEqualTo(11)
    }

    @Test
    fun `counts search results for IDs queries`() {
        adminVideoService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "Apple banana candy"),
                SearchableVideoMetadataFactory.create(id = "2", title = "candy banana apple"),
                SearchableVideoMetadataFactory.create(id = "3", title = "banana apple candy")
            )
        )

        val results = queryServiceVideo.count(
            VideoQuery(
                ids = listOf(
                    "2",
                    "5"
                )
            )
        )

        assertThat(results).isEqualTo(1)
    }

    @Test
    fun `paginates search results`() {
        adminVideoService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", description = "Apple banana candy"),
                SearchableVideoMetadataFactory.create(id = "2", description = "candy banana apple"),
                SearchableVideoMetadataFactory.create(id = "3", description = "candy banana apple"),
                SearchableVideoMetadataFactory.create(id = "4", description = "candy banana apple")
            )
        )

        val results =
            queryServiceVideo.search(PaginatedSearchRequest(query = VideoQuery(
                "banana"
            ), startIndex = 0, windowSize = 2))

        assertThat(results.size).isEqualTo(2)
    }

    @Test
    fun `can retrieve any page`() {
        adminVideoService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", description = "Apple banana candy"),
                SearchableVideoMetadataFactory.create(id = "2", description = "candy banana apple"),
                SearchableVideoMetadataFactory.create(id = "3", description = "candy banana apple"),
                SearchableVideoMetadataFactory.create(id = "4", description = "candy banana apple")
            )
        )

        val page1 = queryServiceVideo.search(PaginatedSearchRequest(query = VideoQuery(
            "banana"
        ), startIndex = 0, windowSize = 2))
        val page2 = queryServiceVideo.search(PaginatedSearchRequest(query = VideoQuery(
            "banana"
        ), startIndex = 2, windowSize = 2))
        val page3 = queryServiceVideo.search(PaginatedSearchRequest(query = VideoQuery(
            "banana"
        ), startIndex = 4, windowSize = 2))

        assertThat(page1).doesNotContainAnyElementsOf(page2)
        assertThat(page1).hasSize(2)
        assertThat(page2).hasSize(2)
        assertThat(page3).hasSize(0)
    }

    @Test
    fun `returns exact matches for IDs search query`() {
        adminVideoService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "Apple banana candy"),
                SearchableVideoMetadataFactory.create(id = "2", title = "candy banana apple"),
                SearchableVideoMetadataFactory.create(id = "3", title = "banana apple candy")
            )
        )

        val results = queryServiceVideo.search(PaginatedSearchRequest(query = VideoQuery(
            ids = listOf("2", "5")
        )
        ))

        assertThat(results).containsExactly("2")
    }

    @Test
    fun `can retrieve just news`() {
        adminVideoService.upsert(
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
            queryServiceVideo.search(PaginatedSearchRequest(query = VideoQuery(
                phrase = "banana",
                includeTags = listOf("news")
            )
            ))

        assertThat(results).containsExactly("4")
    }

    @Test
    fun `can retrieve news that matches query`() {
        adminVideoService.upsert(
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
            queryServiceVideo.search(PaginatedSearchRequest(query = VideoQuery(
                phrase = "banana",
                includeTags = listOf("news")
            )
            ))

        assertThat(results).containsExactly("4")
    }

    @Test
    fun `can retrieve non-news that matches query`() {
        adminVideoService.upsert(
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
            queryServiceVideo.search(PaginatedSearchRequest(query = VideoQuery(
                phrase = "banana",
                excludeTags = listOf("news")
            )
            ))

        assertThat(results).containsExactly("3")
    }

    @Test
    fun `searching with no filters returns news and non-news`() {
        adminVideoService.upsert(
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

        val results = queryServiceVideo.search(PaginatedSearchRequest(query = VideoQuery(
            phrase = "banana"
        )
        ))

        assertThat(results).hasSize(3)
    }

    @Test
    fun `can retrieve educational videos that matches query`() {
        adminVideoService.upsert(
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

        val results = queryServiceVideo.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    phrase = "banana",
                    includeTags = listOf("classroom")
                )
            )
        )

        assertThat(results).containsExactly("9")
    }

    @Test
    fun `can count for just news results`() {
        adminVideoService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "3", description = "candy banana apple"),
                SearchableVideoMetadataFactory.create(
                    id = "4",
                    description = "candy banana apple",
                    tags = listOf("news")
                )
            )
        )

        val results = queryServiceVideo.count(
            VideoQuery(
                phrase = "banana",
                includeTags = listOf("news")
            )
        )

        assertThat(results).isEqualTo(1)
    }

    @Test
    fun `strictly match the include tags`() {
        adminVideoService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "3", description = "banana", tags = listOf("classroom"))
            )
        )

        val results = queryServiceVideo.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    phrase = "banana",
                    includeTags = listOf("classroom", "news")
                )
            )
        )

        assertThat(results).isEmpty()
    }

    @Test
    fun `match any exclude tag`() {
        adminVideoService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "3", description = "banana", tags = listOf("classroom"))
            )
        )

        val results = queryServiceVideo.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    phrase = "banana",
                    excludeTags = listOf("classroom", "news")
                )
            )
        )

        assertThat(results).isEmpty()
    }

    @Test
    fun `having include and exclude as the same tag returns no results`() {
        adminVideoService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "3", description = "banana", tags = listOf("classroom"))
            )
        )

        val results = queryServiceVideo.search(
            PaginatedSearchRequest(
                query = VideoQuery(
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
        adminVideoService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", description = "Second world war")
            )
        )

        val results = queryServiceVideo.search(PaginatedSearchRequest(query = VideoQuery(
            "WW2"
        )
        ))

        assertThat(results).containsExactly("1")
    }

    @Test
    fun `multiword synonyms must match query entirely`() {
        adminVideoService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", description = "video about ww2")
            )
        )

        assertThat(queryServiceVideo.search(PaginatedSearchRequest(query = VideoQuery(
            "second world war"
        )
        ))).containsExactly("1")
        assertThat(queryServiceVideo.search(PaginatedSearchRequest(query = VideoQuery(
            "second world"
        )
        ))).isEmpty()
    }

    @Test
    fun `multiword synonyms must match video metadata entirely`() {
        adminVideoService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", description = "second world")
            )
        )

        assertThat(queryServiceVideo.search(PaginatedSearchRequest(query = VideoQuery(
            "ww2"
        )
        ))).isEmpty()
    }

    @Test
    fun `case sensitive synonyms`() {
        adminVideoService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", description = "Welcome to the US"),
                SearchableVideoMetadataFactory.create(id = "2", description = "Beware of us")
            )
        )

        val results = queryServiceVideo.search(PaginatedSearchRequest(query = VideoQuery(
            "United States of America"
        )
        ))

        assertThat(results).containsExactly("1")
    }
}
