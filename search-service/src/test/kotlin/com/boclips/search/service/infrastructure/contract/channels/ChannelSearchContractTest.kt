package com.boclips.search.service.infrastructure.contract.channels

import com.boclips.search.service.domain.channels.model.CategoryCode
import com.boclips.search.service.domain.channels.model.ChannelMetadata
import com.boclips.search.service.domain.channels.model.ChannelQuery
import com.boclips.search.service.domain.channels.model.IngestType
import com.boclips.search.service.domain.channels.model.Taxonomy
import com.boclips.search.service.domain.common.IndexReader
import com.boclips.search.service.domain.common.IndexWriter
import com.boclips.search.service.domain.common.model.PaginatedIndexSearchRequest
import com.boclips.search.service.domain.common.model.Sort
import com.boclips.search.service.domain.common.model.SortOrder
import com.boclips.search.service.infrastructure.channels.ChannelsIndexReader
import com.boclips.search.service.infrastructure.channels.ChannelsIndexWriter
import com.boclips.search.service.infrastructure.contract.ChannelIndexFake
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableChannelMetadataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import java.util.stream.Stream

class SearchServiceProvider : ArgumentsProvider {
    override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {
        val inMemorySearchService = ChannelIndexFake()
        val elasticSearchService = ChannelsIndexReader(
            EmbeddedElasticSearchIntegrationTest.CLIENT.buildClient()
        )
        val elasticSearchServiceAdmin =
            ChannelsIndexWriter.createTestInstance(
                EmbeddedElasticSearchIntegrationTest.CLIENT.buildClient(),
                100
            )

        return Stream.of(
            Arguments.of(inMemorySearchService, inMemorySearchService),
            Arguments.of(elasticSearchService, elasticSearchServiceAdmin)
        )
    }
}

class ChannelSearchContractTest : EmbeddedElasticSearchIntegrationTest() {
    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `returns everything when an empty query is specified`(
        queryService: IndexReader<ChannelMetadata, ChannelQuery>,
        adminService: IndexWriter<ChannelMetadata>
    ) {
        adminService.safeRebuildIndex(
            sequenceOf(
                SearchableChannelMetadataFactory.create(
                    id = "1",
                    name = "Apes Channel"
                )
            )
        )

        val result = queryService.search(PaginatedIndexSearchRequest(query = ChannelQuery()))

        assertThat(result.elements).containsExactly("1")
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `returns channels sorted by taxonomy categories ASC`(
        queryService: IndexReader<ChannelMetadata, ChannelQuery>,
        adminService: IndexWriter<ChannelMetadata>
    ) {
        adminService.safeRebuildIndex(
            sequenceOf(
                SearchableChannelMetadataFactory.create(
                    id = "1", name = "untagged, needs video level tagging",
                    taxonomy = Taxonomy(videoLevelTagging = true, categories = null)
                ),
                SearchableChannelMetadataFactory.create(
                    id = "2",
                    name = "untagged, does not need video level tagging",
                    taxonomy = Taxonomy(videoLevelTagging = false, categories = emptySet())
                ),
                SearchableChannelMetadataFactory.create(
                    id = "3", name = "tagged, first category is C",
                    taxonomy = Taxonomy(
                        videoLevelTagging = false,
                        categories = setOf(CategoryCode("G"), CategoryCode("DEF"), CategoryCode("C"))
                    )
                ),
                SearchableChannelMetadataFactory.create(
                    id = "4", name = "tagged, first category is ABC",
                    taxonomy = Taxonomy(
                        videoLevelTagging = false,
                        categories = setOf(
                            CategoryCode("Z"), CategoryCode("DEF"), CategoryCode("ABC")
                        )
                    )
                ),
            )
        )

        val results = queryService.search(
            PaginatedIndexSearchRequest(
                query = ChannelQuery(
                    sort = listOf(Sort.ByField(fieldName = ChannelMetadata::taxonomy, order = SortOrder.ASC))
                )
            )
        )

        assertThat(results.elements.size).isEqualTo(4)
        assertThat(results.elements).containsExactly("2", "1", "4", "3")
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `returns channels sorted by taxonomy categories DESC`(
        queryService: IndexReader<ChannelMetadata, ChannelQuery>,
        adminService: IndexWriter<ChannelMetadata>
    ) {
        adminService.safeRebuildIndex(
            sequenceOf(
                SearchableChannelMetadataFactory.create(
                    id = "1", name = "untagged, needs video level tagging",
                    taxonomy = Taxonomy(videoLevelTagging = true, categories = null)
                ),
                SearchableChannelMetadataFactory.create(
                    id = "2",
                    name = "untagged, does not need video level tagging",
                    taxonomy = Taxonomy(videoLevelTagging = false, categories = emptySet())
                ),
                SearchableChannelMetadataFactory.create(
                    id = "3", name = "tagged, first category is C",
                    taxonomy = Taxonomy(
                        videoLevelTagging = false,
                        categories = setOf(CategoryCode("G"), CategoryCode("DEF"), CategoryCode("C"))
                    )
                ),
                SearchableChannelMetadataFactory.create(
                    id = "4", name = "tagged, first category is ABC",
                    taxonomy = Taxonomy(
                        videoLevelTagging = false,
                        categories = setOf(
                            CategoryCode("Z"), CategoryCode("DEF"), CategoryCode("ABC")
                        )
                    )
                ),
            )
        )

        val results = queryService.search(
            PaginatedIndexSearchRequest(
                query = ChannelQuery(
                    sort = listOf(Sort.ByField(fieldName = ChannelMetadata::taxonomy, order = SortOrder.DESC))
                )
            )
        )

        assertThat(results.elements.size).isEqualTo(4)
        assertThat(results.elements).containsExactly("3", "4", "1", "2")
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `returns only the requested page of results`(
        queryService: IndexReader<ChannelMetadata, ChannelQuery>,
        adminService: IndexWriter<ChannelMetadata>
    ) {
        adminService.safeRebuildIndex(
            sequenceOf(
                SearchableChannelMetadataFactory.create(id = "1"),
                SearchableChannelMetadataFactory.create(id = "2"),
                SearchableChannelMetadataFactory.create(id = "3"),
                SearchableChannelMetadataFactory.create(id = "4")
            ),
        )

        val page1 =
            queryService.search(PaginatedIndexSearchRequest(query = ChannelQuery(), startIndex = 0, windowSize = 2))
        val page2 =
            queryService.search(PaginatedIndexSearchRequest(query = ChannelQuery(), startIndex = 2, windowSize = 2))

        assertThat(page1.elements).containsExactly("1", "2")
        assertThat(page1.counts.totalHits).isEqualTo(4)

        assertThat(page2.elements).containsExactly("3", "4")
        assertThat(page2.counts.totalHits).isEqualTo(4)
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `can filter by ingest type`(
        queryService: IndexReader<ChannelMetadata, ChannelQuery>,
        adminService: IndexWriter<ChannelMetadata>
    ) {
        adminService.safeRebuildIndex(
            sequenceOf(
                SearchableChannelMetadataFactory.create(id = "1", ingestType = IngestType.MRSS),
                SearchableChannelMetadataFactory.create(id = "2", ingestType = IngestType.MANUAL),
                SearchableChannelMetadataFactory.create(id = "3", ingestType = IngestType.YOUTUBE),
            ),
        )

        val results = queryService.search(
            PaginatedIndexSearchRequest(
                query = ChannelQuery(
                    ingestTypes = listOf(
                        IngestType.YOUTUBE,
                        IngestType.MANUAL
                    )
                ), startIndex = 0, windowSize = 2
            )
        )

        assertThat(results.elements).containsExactly("2", "3")
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `channels are sorted by default by name`(
        queryService: IndexReader<ChannelMetadata, ChannelQuery>,
        adminService: IndexWriter<ChannelMetadata>
    ) {
        adminService.safeRebuildIndex(
            sequenceOf(
                SearchableChannelMetadataFactory.create(id = "1", name = "Z"),
                SearchableChannelMetadataFactory.create(id = "2", name = "B"),
                SearchableChannelMetadataFactory.create(id = "3", name = "1"),
                SearchableChannelMetadataFactory.create(id = "4", name = "BB"),
            ),
        )

        val results = queryService.search(
            PaginatedIndexSearchRequest(
                query = ChannelQuery(), startIndex = 0, windowSize = 4
            )
        )

        assertThat(results.elements).containsExactly("3", "2", "4", "1")
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `can sort by name ascending`(
        queryService: IndexReader<ChannelMetadata, ChannelQuery>,
        adminService: IndexWriter<ChannelMetadata>
    ) {
        adminService.safeRebuildIndex(
            sequenceOf(
                SearchableChannelMetadataFactory.create(id = "1", name = "Z"),
                SearchableChannelMetadataFactory.create(id = "2", name = "B"),
                SearchableChannelMetadataFactory.create(id = "3", name = "1"),
                SearchableChannelMetadataFactory.create(id = "4", name = "BB"),
            ),
        )

        val results = queryService.search(
            PaginatedIndexSearchRequest(
                query = ChannelQuery(
                    sort = listOf(Sort.ByField(fieldName = ChannelMetadata::name, order = SortOrder.ASC))
                ), startIndex = 0, windowSize = 4
            )
        )

        assertThat(results.elements).containsExactly("3", "2", "4", "1")
    }
}
