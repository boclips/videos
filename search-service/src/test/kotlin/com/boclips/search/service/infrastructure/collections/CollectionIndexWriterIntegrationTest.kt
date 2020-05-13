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
        indexWriter = CollectionIndexWriter.createTestInstance(esClient, 20)
    }

    @Test
    fun `creates a new index and upserts the collections provided`() {
        indexWriter.safeRebuildIndex(
            sequenceOf(SearchableCollectionMetadataFactory.create(id = "1", title = "Beautiful Boy Dancing"))
        )

        val results = indexReader.search(PaginatedSearchRequest(query = CollectionQuery("Boy")))

        assertThat(results.counts.totalHits).isEqualTo(1)
    }

    @Test
    fun `creates a new index and upserts the collections provided with description`() {
        indexWriter.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(
                    id = "1",
                    title = "Beautiful Boy Dancing",
                    description = "A verbose description about this collection"
                )
            )
        )

        val results = indexReader.search(PaginatedSearchRequest(query = CollectionQuery("verbose")))

        assertThat(results.counts.totalHits).isEqualTo(1)
    }

    @Test
    fun `creates a new index and upserts the collections provided with attachment type`() {
        indexWriter.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(
                    id = "1",
                    title = "Beautiful Boy Dancing",
                    description = "A verbose description about this collection"
                ) ,
                SearchableCollectionMetadataFactory.create(
                    id = "2",
                    title = "Beautiful Boy Dancing",
                    description = "A verbose description about this collection",
                    attachmentTypes = setOf("Lesson Guide")
                )
            )
        )

        val results = indexReader.search(
            PaginatedSearchRequest(
                query = CollectionQuery(phrase = "Dancing", resourceTypes = setOf("Lesson Guide"))
            )
        )

        assertThat(results.counts.totalHits).isEqualTo(1)
        assertThat(results.elements.first()).isEqualTo("2")
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
            ).elements
        ).isNotEmpty

        indexWriter.safeRebuildIndex(emptySequence())

        assertThat(
            indexReader.search(
                PaginatedSearchRequest(
                    query = CollectionQuery(
                        "boy"
                    )
                )
            ).elements.isEmpty()
        )
    }
}
