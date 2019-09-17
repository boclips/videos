package com.boclips.search.service.infrastructure.collections

import com.boclips.search.service.domain.collections.model.CollectionQuery
import com.boclips.search.service.domain.collections.model.CollectionVisibility
import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableCollectionMetadataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CollectionIndexReaderVisibilitySearchIntegrationTest : EmbeddedElasticSearchIntegrationTest() {
    lateinit var indexReader: CollectionIndexReader
    lateinit var indexWriter: CollectionIndexWriter

    @BeforeEach
    fun setUp() {
        indexReader = CollectionIndexReader(esClient)
        indexWriter = CollectionIndexWriter(esClient)
    }

    @Test
    fun `searching without visibility set returns collections with any visibility`() {
        indexWriter.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(
                    id = "1",
                    title = "Beautiful Boy Dancing",
                    visibility = CollectionVisibility.PUBLIC
                ),
                SearchableCollectionMetadataFactory.create(
                    id = "2",
                    title = "Beautiful Boy Dancing",
                    visibility = CollectionVisibility.PRIVATE
                )
            )
        )

        assertThat(indexReader.count(CollectionQuery("Boy"))).isEqualTo(2)
    }

    @Test
    fun `searching with visibility only set returns collections with given visibility`() {
        indexWriter.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(
                    id = "1",
                    title = "Beautiful Boy Dancing",
                    visibility = CollectionVisibility.PUBLIC
                ),
                SearchableCollectionMetadataFactory.create(
                    id = "2",
                    title = "Beautiful Boy Dancing",
                    visibility = CollectionVisibility.PRIVATE
                )
            )
        )

        assertThat(
            indexReader.search(
                PaginatedSearchRequest(
                    CollectionQuery(
                        visibility = CollectionVisibility.PUBLIC
                    )
                )
            )
        ).containsExactly("1")
    }

    @Test
    fun `searching with phrase and visibility set returns expected results`() {
        indexWriter.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(
                    id = "1",
                    title = "Beautiful Boy Dancing",
                    visibility = CollectionVisibility.PUBLIC
                ),
                SearchableCollectionMetadataFactory.create(
                    id = "2",
                    title = "Beautiful Boy Dancing",
                    visibility = CollectionVisibility.PRIVATE
                )
            )
        )

        assertThat(
            indexReader.search(
                PaginatedSearchRequest(
                    CollectionQuery(
                        phrase = "Boy",
                        visibility = CollectionVisibility.PUBLIC
                    )
                )
            )
        ).containsExactly("1")
    }

    @Test
    fun `searching with subjects and visibility set returns expected results`() {
        indexWriter.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(
                    id = "1",
                    title = "Beautiful Boy Dancing",
                    visibility = CollectionVisibility.PUBLIC,
                    subjects = listOf("Math")
                ),
                SearchableCollectionMetadataFactory.create(
                    id = "2",
                    title = "Beautiful Boy Dancing",
                    visibility = CollectionVisibility.PUBLIC,
                    subjects = listOf("Physics")
                )
            )
        )

        assertThat(
            indexReader.search(
                PaginatedSearchRequest(
                    CollectionQuery(
                        visibility = CollectionVisibility.PUBLIC,
                        subjectIds = listOf("Math")
                    )
                )
            )
        ).containsExactly("1")
    }
}