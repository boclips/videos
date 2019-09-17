package com.boclips.search.service.infrastructure.collections

import com.boclips.search.service.domain.collections.model.CollectionQuery
import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableCollectionMetadataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CollectionIndexWriterIntegrationTest : EmbeddedElasticSearchIntegrationTest() {
    lateinit var indexReader: CollectionIndexReader
    lateinit var indexWriter: CollectionIndexWriter

    @BeforeEach
    fun setUp() {
        indexReader = CollectionIndexReader(esClient)
        indexWriter = CollectionIndexWriter(esClient)
    }

    @Test
    fun `creates a new index and upserts the collections provided`() {
        indexWriter.safeRebuildIndex(
            sequenceOf(SearchableCollectionMetadataFactory.create(id = "1", title = "Beautiful Boy Dancing"))
        )

        assertThat(indexReader.count(CollectionQuery("Boy"))).isEqualTo(1)
    }

    @Test
    fun `creates a new index and removes the outdated one`() {
        indexWriter.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(
                    id = "1",
                    title = "Beautiful Boy Dancing"
                )
            )
        )
        assertThat(
            indexReader.search(
                PaginatedSearchRequest(
                    query = CollectionQuery(
                        "boy"
                    )
                )
            ).isNotEmpty()
        )

        indexWriter.safeRebuildIndex(emptySequence())

        assertThat(
            indexReader.search(
                PaginatedSearchRequest(
                    query = CollectionQuery(
                        "boy"
                    )
                )
            ).isEmpty()
        )
    }
}