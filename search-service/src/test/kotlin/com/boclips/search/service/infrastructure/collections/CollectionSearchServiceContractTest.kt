package com.boclips.search.service.infrastructure.collections

import com.boclips.search.service.domain.ReadSearchService
import com.boclips.search.service.domain.WriteSearchService
import com.boclips.search.service.domain.collections.model.CollectionMetadata
import com.boclips.search.service.domain.collections.model.CollectionQuery
import com.boclips.search.service.domain.model.PaginatedSearchRequest
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableCollectionMetadataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import java.util.stream.Stream

class SearchServiceProvider : ArgumentsProvider {
    override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {
        val inMemorySearchService = InMemoryCollectionSearchService()
        val esClient = EmbeddedElasticSearchIntegrationTest.CONFIG.buildClient()
        val readSearchService = ESCollectionReadSearchService(esClient)
        val writeSearchService = ESCollectionWriteSearchService(esClient)

        return Stream.of(
            Arguments.of(inMemorySearchService, inMemorySearchService),
            Arguments.of(readSearchService, writeSearchService)
        )
    }
}

class SearchServiceContractTest : EmbeddedElasticSearchIntegrationTest() {

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `returns empty collection for empty result`(
        readService: ReadSearchService<CollectionMetadata, CollectionQuery>,
        writeService: WriteSearchService<CollectionMetadata>
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
    @ArgumentsSource(SearchServiceProvider::class)
    fun `finds a collection matching metadata`(
        readService: ReadSearchService<CollectionMetadata, CollectionQuery>,
        writeService: WriteSearchService<CollectionMetadata>
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
            PaginatedSearchRequest(
                query = CollectionQuery(
                    "gentleman"
                )
            )
        )

        assertThat(result).containsExactlyInAnyOrder("1", "3")
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `paginates results`(
        readService: ReadSearchService<CollectionMetadata, CollectionQuery>,
        writeService: WriteSearchService<CollectionMetadata>
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
    @ArgumentsSource(SearchServiceProvider::class)
    fun `counts all collections matching metadata`(
        readService: ReadSearchService<CollectionMetadata, CollectionQuery>,
        writeService: WriteSearchService<CollectionMetadata>
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
    @ArgumentsSource(SearchServiceProvider::class)
    fun `removed collections are not searchable`(
        readService: ReadSearchService<CollectionMetadata, CollectionQuery>,
        writeService: WriteSearchService<CollectionMetadata>
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

        assertThat(readService.search(
            PaginatedSearchRequest(
                query = CollectionQuery(
                    "gentleman"
                )
            )
        ).isEmpty())
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `creates a new index and removes the outdated one`(
        readService: ReadSearchService<CollectionMetadata, CollectionQuery>,
        writeService: WriteSearchService<CollectionMetadata>
    ) {
        writeService.safeRebuildIndex(
            sequenceOf(
                SearchableCollectionMetadataFactory.create(
                    id = "1",
                    title = "Beautiful Boy Dancing"
                )
            )
        )
        assertThat(readService.search(
            PaginatedSearchRequest(
                query = CollectionQuery(
                    "boy"
                )
            )
        ).isNotEmpty())

        writeService.safeRebuildIndex(emptySequence())

        assertThat(readService.search(
            PaginatedSearchRequest(
                query = CollectionQuery(
                    "boy"
                )
            )
        ).isEmpty())
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `creates a new index and upserts the collections provided`(
        readService: ReadSearchService<CollectionMetadata, CollectionQuery>,
        writeService: WriteSearchService<CollectionMetadata>
    ) {
        writeService.safeRebuildIndex(
            sequenceOf(SearchableCollectionMetadataFactory.create(id = "1", title = "Beautiful Boy Dancing"))
        )

        assertThat(readService.count(CollectionQuery("Boy"))).isEqualTo(1)
    }
}
