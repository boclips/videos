package com.boclips.search.service.domain

import com.boclips.search.service.infrastructure.InMemorySearchService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

class TestSearchService(query: GenericSearchService, admin: GenericSearchServiceAdmin<VideoMetadata>) :
    SearchServiceAdapter<String>(query, admin) {
    override fun convert(document: String): VideoMetadata {
        return VideoMetadata(
            id = document.substring(0, 1).toUpperCase(),
            title = document,
            description = "",
            contentProvider = "",
            releaseDate = LocalDate.now(),
            keywords = emptyList(),
            tags = listOf("classroom")
        )
    }
}

class SearchServiceAdapterTest {
    lateinit var searchService: TestSearchService

    @BeforeEach
    internal fun setUp() {
        val inMemorySearchService = InMemorySearchService()
        searchService = TestSearchService(inMemorySearchService, inMemorySearchService)
    }

    @Test
    fun `upsert one video makes an insert`() {
        searchService.upsert(sequenceOf("hello"))

        val result = searchService.search(PaginatedSearchRequest(Query("hello"), 0, 1)).first()

        assertThat(result).isEqualTo("H")
    }

    @Test
    fun `upsert many videos makes an insert`() {
        searchService.upsert(sequenceOf("one", "two"))

        val result = searchService.search(PaginatedSearchRequest(Query("two"), 0, 1)).first()

        assertThat(result).isEqualTo("T")
    }

    @Test
    fun `safeRebuildIndex clears the index`() {
        searchService.upsert(sequenceOf("hello"))
        searchService.safeRebuildIndex(emptySequence())

        assertThat(searchService.search(PaginatedSearchRequest(Query("hello"), 0, 1))).isEmpty()
    }

    @Test
    fun `count returns document count`() {
        searchService.upsert(sequenceOf("one", "two one"))

        assertThat(searchService.count(Query("one"))).isEqualTo(2)
    }

    @Test
    fun `removeFromSearch removes from the index`() {
        searchService.upsert(sequenceOf("hello"))
        searchService.removeFromSearch("H")

        assertThat(searchService.search(PaginatedSearchRequest(Query("hello"), 0, 1))).isEmpty()
    }
}
