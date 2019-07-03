package com.boclips.search.service.infrastructure.fakes

import com.boclips.search.service.domain.collections.model.CollectionMetadata
import com.boclips.search.service.domain.collections.model.CollectionQuery
import com.boclips.search.service.domain.common.IndexReader
import com.boclips.search.service.domain.common.IndexWriter
import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.infrastructure.collections.CollectionIndexReader
import com.boclips.search.service.infrastructure.collections.CollectionIndexWriter
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableCollectionMetadataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import java.util.stream.Stream

class CollectionSearchProvider : ArgumentsProvider {
    override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {
        val inMemorySearchService = CollectionSearchServiceFake()

        val esClient = EmbeddedElasticSearchIntegrationTest.CLIENT.buildClient()
        val readSearchService = CollectionIndexReader(esClient)
        val writeSearchService = CollectionIndexWriter(esClient)

        return Stream.of(
            Arguments.of(inMemorySearchService, inMemorySearchService),
            Arguments.of(readSearchService, writeSearchService)
        )
    }
}

class CollectionSearchServiceContractTest : EmbeddedElasticSearchIntegrationTest() {
    @ParameterizedTest
    @ArgumentsSource(CollectionSearchProvider::class)
    fun `returns empty collection when nothing found`(
        readService: IndexReader<CollectionMetadata, CollectionQuery>,
        writeService: IndexWriter<CollectionMetadata>
    ) {
        writeService.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(
                    id = "1",
                    title = "White Gentleman Dancing"
                )
            )
        )

        val result = readService.search(
            PaginatedSearchRequest(
                query = CollectionQuery(
                    "collectionQuery that matches nothing"
                )
            )
        )

        assertThat(result).hasSize(0)
    }

    @ParameterizedTest
    @ArgumentsSource(CollectionSearchProvider::class)
    fun `finds a collection matching title`(
        readService: IndexReader<CollectionMetadata, CollectionQuery>,
        writeService: IndexWriter<CollectionMetadata>
    ) {
        writeService.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(id = "1", title = "White Gentleman Dancing"),
                SearchableCollectionMetadataFactory.create(
                    id = "2",
                    title = "Beer"
                ),
                SearchableCollectionMetadataFactory.create(
                    id = "3",
                    title = "Mixed-race couple playing piano with a dog and a gentleman"
                ),
                SearchableCollectionMetadataFactory.create(
                    id = "4",
                    title = "Who are you, really?"
                )
            )
        )

        val result = readService.search(
            PaginatedSearchRequest(query = CollectionQuery("gentleman"))
        )

        assertThat(result).containsExactlyInAnyOrder("1", "3")
    }

    @ParameterizedTest
    @ArgumentsSource(CollectionSearchProvider::class)
    fun `finds a collection matching subjects`(
        readService: IndexReader<CollectionMetadata, CollectionQuery>,
        writeService: IndexWriter<CollectionMetadata>
    ) {
        writeService.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(id = "1", subjects = listOf("crispity", "crunchy")),
                SearchableCollectionMetadataFactory.create(id = "2", subjects = listOf("crunchy")),
                SearchableCollectionMetadataFactory.create(id = "3", subjects = emptyList())
            )
        )

        val result = readService.search(
            PaginatedSearchRequest(query = CollectionQuery(subjectIds = listOf("gentleman", "crispity")))
        )

        assertThat(result).containsExactlyInAnyOrder("1")
    }

    @ParameterizedTest
    @ArgumentsSource(CollectionSearchProvider::class)
    fun `gets all collections when empty query`(
        readService: IndexReader<CollectionMetadata, CollectionQuery>,
        writeService: IndexWriter<CollectionMetadata>
    ) {
        writeService.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(id = "1", title = "White Gentleman Dancing"),
                SearchableCollectionMetadataFactory.create(id = "2", title = "Beer")
            )
        )

        val result = readService.search(
            PaginatedSearchRequest(
                query = CollectionQuery(phrase = "")
            )
        )

        assertThat(result).containsExactlyInAnyOrder("1", "2")
    }

    @ParameterizedTest
    @ArgumentsSource(CollectionSearchProvider::class)
    fun `paginates results`(
        readService: IndexReader<CollectionMetadata, CollectionQuery>,
        writeService: IndexWriter<CollectionMetadata>
    ) {
        writeService.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(id = "1", title = "White Gentleman Dancing"),
                SearchableCollectionMetadataFactory.create(
                    id = "2",
                    title = "Beer"
                ),
                SearchableCollectionMetadataFactory.create(
                    id = "3",
                    title = "Mixed-race couple playing piano with a dog and a gentleman"
                ),
                SearchableCollectionMetadataFactory.create(
                    id = "4",
                    title = "Who are you, really? I am GENTLEman"
                )
            )
        )

        val page1 =
            readService.search(
                PaginatedSearchRequest(
                    query = CollectionQuery(
                        "gentleman"
                    ), startIndex = 0, windowSize = 2
                )
            )
        val page2 =
            readService.search(
                PaginatedSearchRequest(
                    query = CollectionQuery(
                        "gentleman"
                    ), startIndex = 2, windowSize = 2
                )
            )

        assertThat(page1).hasSize(2)
        assertThat(page2).hasSize(1)
    }

    @ParameterizedTest
    @ArgumentsSource(CollectionSearchProvider::class)
    fun `counts collections`(
        readService: IndexReader<CollectionMetadata, CollectionQuery>,
        writeService: IndexWriter<CollectionMetadata>
    ) {
        writeService.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(id = "1", title = "White Gentleman Dancing"),
                SearchableCollectionMetadataFactory.create(
                    id = "2",
                    title = "Beer"
                ),
                SearchableCollectionMetadataFactory.create(
                    id = "3",
                    title = "Mixed-race couple playing piano with a dog and a gentleman"
                ),
                SearchableCollectionMetadataFactory.create(
                    id = "4",
                    title = "Who are you, really? I am GENTLEman"
                )
            )
        )

        val result = readService.count(CollectionQuery("gentleman"))

        assertThat(result).isEqualTo(3)
    }

    @ParameterizedTest
    @ArgumentsSource(CollectionSearchProvider::class)
    fun `removed collections are not searchable`(
        readService: IndexReader<CollectionMetadata, CollectionQuery>,
        writeService: IndexWriter<CollectionMetadata>
    ) {
        writeService.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(
                    id = "1",
                    title = "White Gentleman Dancing"
                )
            )
        )

        writeService.removeFromSearch("1")

        assertThat(
            readService.search(
                PaginatedSearchRequest(
                    query = CollectionQuery(
                        "gentleman"
                    )
                )
            ).isEmpty()
        )
    }

    @ParameterizedTest
    @ArgumentsSource(CollectionSearchProvider::class)
    fun `can bulk remove collections from index`(
        readService: IndexReader<CollectionMetadata, CollectionQuery>,
        writeService: IndexWriter<CollectionMetadata>
    ) {
        writeService.safeRebuildIndex(
            sequenceOf(

                SearchableCollectionMetadataFactory.create(
                    id = "1",
                    title = "White Gentleman Dancing"
                ),
                SearchableCollectionMetadataFactory.create(
                    id = "2",
                    title = "White Gentleman Dancing"
                ),
                SearchableCollectionMetadataFactory.create(
                    id = "3",
                    title = "White Gentleman Dancing"
                )
            )
        )

        writeService.bulkRemoveFromSearch(listOf("1", "2", "3"))

        assertThat(
            readService.search(
                PaginatedSearchRequest(
                    query = CollectionQuery(
                        "gentleman"
                    )
                )
            ).isEmpty()
        )
    }
}
