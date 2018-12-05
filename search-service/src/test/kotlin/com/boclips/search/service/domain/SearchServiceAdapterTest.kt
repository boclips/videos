package com.boclips.search.service.domain

import com.boclips.search.service.infrastructure.InMemorySearchService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TestSearchService(inner: GenericSearchService<VideoMetadata>) : SearchServiceAdapter<String>(inner) {
    override fun convert(document: String): VideoMetadata {
        return VideoMetadata(
                id = document.substring(0,1).toUpperCase(),
                title = document,
                description = "",
                contentProvider = "",
                keywords = emptyList()
        )
    }
}

class SearchServiceAdapterTest {

    lateinit var searchService: TestSearchService

    @BeforeEach
    internal fun setUp() {
        val inner = InMemorySearchService()
        searchService = TestSearchService(inner)
    }

    @Test
    fun `upsert one video makes an insert`() {
        searchService.upsert(sequenceOf("hello"))

        val result = searchService.search(PaginatedSearchRequest("hello", 0, 1)).first()

        assertThat(result).isEqualTo("H")
    }

    @Test
    fun `upsert many videos makes an insert`() {
        searchService.upsert(sequenceOf("one", "two"))

        val result = searchService.search(PaginatedSearchRequest("two", 0, 1)).first()

        assertThat(result).isEqualTo("T")
    }

    @Test
    fun `resetIndex clears the index`() {
        searchService.upsert(sequenceOf("hello"))
        searchService.resetIndex()

        assertThat(searchService.search(PaginatedSearchRequest("hello", 0, 1))).isEmpty()
    }

    @Test
    fun `count returns document count`() {
        searchService.upsert(sequenceOf("one", "two one"))

        assertThat(searchService.count("one")).isEqualTo(2)
    }

    @Test
    fun `removeFromSearch removes from the index`() {
        searchService.upsert(sequenceOf("hello"))
        searchService.removeFromSearch("H")

        assertThat(searchService.search(PaginatedSearchRequest("hello", 0, 1))).isEmpty()

    }
}