package com.boclips.search.service.domain.collections

import com.boclips.search.service.domain.ReadSearchService
import com.boclips.search.service.domain.WriteSearchService
import com.boclips.search.service.domain.collections.model.CollectionMetadata
import com.boclips.search.service.domain.collections.model.CollectionQuery
import com.boclips.search.service.domain.model.PaginatedSearchRequest
import com.boclips.search.service.infrastructure.collections.InMemoryCollectionSearchService
import com.boclips.search.service.testsupport.SearchableCollectionMetadataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TestCollectionSearchService(
    query: ReadSearchService<CollectionMetadata, CollectionQuery>,
    admin: WriteSearchService<CollectionMetadata>
) :
    CollectionSearchServiceAdapter<String>(query, admin) {
    override fun convert(document: String): CollectionMetadata {
        return SearchableCollectionMetadataFactory.create(
            id = document.substring(0, 1).toUpperCase(),
            title = document
        )
    }
}

class CollectionSearchServiceAdapterTest {
    lateinit var searchService: TestCollectionSearchService

    @BeforeEach
    internal fun setUp() {
        val inMemorySearchService = InMemoryCollectionSearchService()
        searchService = TestCollectionSearchService(
            inMemorySearchService,
            inMemorySearchService
        )
    }

    @Test
    fun `upsert one collection makes an insert`() {
        searchService.upsert(sequenceOf("hello"))

        val result = searchService.search(
            PaginatedSearchRequest(
                CollectionQuery(
                    "hello"
                ), 0, 1
            )
        ).first()

        assertThat(result).isEqualTo("H")
    }

    @Test
    fun `upsert many collections makes an insert`() {
        searchService.upsert(sequenceOf("one", "two"))

        val result = searchService.search(
            PaginatedSearchRequest(
                CollectionQuery(
                    "two"
                ), 0, 1
            )
        ).first()

        assertThat(result).isEqualTo("T")
    }

    @Test
    fun `safeRebuildIndex clears the index`() {
        searchService.upsert(sequenceOf("hello"))
        searchService.safeRebuildIndex(emptySequence())

        assertThat(
            searchService.search(
                PaginatedSearchRequest(
                    CollectionQuery(
                        "hello"
                    ), 0, 1
                )
            )
        ).isEmpty()
    }

    @Test
    fun `count returns document count`() {
        searchService.upsert(sequenceOf("one", "two one"))

        assertThat(searchService.count(CollectionQuery("one"))).isEqualTo(2)
    }

    @Test
    fun `removeFromSearch removes from the index`() {
        searchService.upsert(sequenceOf("hello"))
        searchService.removeFromSearch("H")

        assertThat(
            searchService.search(
                PaginatedSearchRequest(
                    CollectionQuery(
                        "hello"
                    ), 0, 1
                )
            )
        ).isEmpty()
    }
}
