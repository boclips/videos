package com.boclips.search.service.infrastructure.channels

import com.boclips.search.service.domain.channels.model.CategoryCode
import com.boclips.search.service.domain.channels.model.ChannelMetadata
import com.boclips.search.service.domain.channels.model.SuggestionAccessRuleQuery
import com.boclips.search.service.domain.channels.model.SuggestionQuery
import com.boclips.search.service.domain.channels.model.Taxonomy
import com.boclips.search.service.domain.common.model.SuggestionsSearchRequest
import com.boclips.search.service.domain.common.model.Sort
import com.boclips.search.service.domain.common.model.SortOrder
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableChannelMetadataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ChannelsIndexReaderIntegrationTest : EmbeddedElasticSearchIntegrationTest() {
    lateinit var indexReader: ChannelsIndexReader
    lateinit var indexWriter: ChannelsIndexWriter

    @BeforeEach
    fun setUp() {
        indexReader = ChannelsIndexReader(esClient)
        indexWriter = ChannelsIndexWriter.createTestInstance(esClient, 20)
    }

    @Nested
    inner class Pagination {
        @Test
        fun `returns only a requested page of records`() {
            indexWriter.safeRebuildIndex(
                sequenceOf(
                    SearchableChannelMetadataFactory.create(
                        id = "1", name = "1",
                    ),
                    SearchableChannelMetadataFactory.create(
                        id = "2", name = "2",
                    ),
                    SearchableChannelMetadataFactory.create(
                        id = "3", name = "3",
                    ),
                    SearchableChannelMetadataFactory.create(
                        id = "4", name = "4",
                    ),
                )
            )

            val pageNumber = 2
            val pageSize = 2

            val results = indexReader.search(
                    pagination = ChannelsPagination(pageNumber = pageNumber, pageSize = pageSize)
                )
            )

            assertThat(results.elements.size).isEqualTo(2)
            assertThat(results.elements[0].id).isEqualTo("3")
            assertThat(results.elements[1].id).isEqualTo("4")
        }
    }

    @Nested
    inner class Sorting {

        @Test
        fun `returns channels sorted by taxonomy categories ASC`() {
            indexWriter.safeRebuildIndex(
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

            val results = indexReader.search(
                SuggestionsSearchRequest(
                    query = SuggestionQuery(
                        accessRuleQuery = SuggestionAccessRuleQuery(),
                        sort = listOf(Sort.ByField(fieldName = ChannelMetadata::taxonomy, order = SortOrder.ASC))
                    )
                )
            )

            assertThat(results.elements.size).isEqualTo(4)
            assertThat(results.elements[0].id).isEqualTo("2")
            assertThat(results.elements[0].name).isEqualTo("untagged, does not need video level tagging")
            assertThat(results.elements[1].id).isEqualTo("1")
            assertThat(results.elements[1].name).isEqualTo("untagged, needs video level tagging")
            assertThat(results.elements[2].id).isEqualTo("4")
            assertThat(results.elements[2].name).isEqualTo("tagged, first category is ABC")
            assertThat(results.elements[3].id).isEqualTo("3")
            assertThat(results.elements[3].name).isEqualTo("tagged, first category is C")
        }

        @Test
        fun `returns channels sorted by taxonomy categories DESC`() {
            indexWriter.safeRebuildIndex(
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

            val results = indexReader.search(
                SuggestionsSearchRequest(
                    query = SuggestionQuery(
                        accessRuleQuery = SuggestionAccessRuleQuery(),
                        sort = listOf(Sort.ByField(fieldName = ChannelMetadata::taxonomy, order = SortOrder.DESC))
                    )
                )
            )

            assertThat(results.elements.size).isEqualTo(4)
            assertThat(results.elements[0].id).isEqualTo("3")
            assertThat(results.elements[0].name).isEqualTo("tagged, first category is C")
            assertThat(results.elements[1].id).isEqualTo("4")
            assertThat(results.elements[1].name).isEqualTo("tagged, first category is ABC")
            assertThat(results.elements[2].id).isEqualTo("1")
            assertThat(results.elements[2].name).isEqualTo("untagged, needs video level tagging")
            assertThat(results.elements[3].id).isEqualTo("2")
            assertThat(results.elements[3].name).isEqualTo("untagged, does not need video level tagging")
        }
    }

}
