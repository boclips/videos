package com.boclips.search.service.infrastructure.channels

import com.boclips.search.service.domain.channels.model.SuggestionAccessRuleQuery
import com.boclips.search.service.domain.channels.model.SuggestionQuery
import com.boclips.search.service.domain.common.model.SuggestionRequest
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableChannelMetadataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ChannelsIndexWriterIntegrationTest : EmbeddedElasticSearchIntegrationTest() {
    lateinit var indexReader: ChannelsIndexReader
    lateinit var indexWriter: ChannelsIndexWriter

    @BeforeEach
    fun setUp() {
        indexReader = ChannelsIndexReader(esClient)
        indexWriter = ChannelsIndexWriter.createTestInstance(esClient, 20)
    }

    @Test
    fun `creates a new index and upserts the channel provided`() {
        indexWriter.safeRebuildIndex(
            sequenceOf(SearchableChannelMetadataFactory.create(id = "1", name = "Super Channel"))
        )

        val results = indexReader.getSuggestions(
            SuggestionRequest(
                query = SuggestionQuery(
                    "super",
                    SuggestionAccessRuleQuery(includedChannelIds = setOf("1"))
                )
            )
        )

        assertThat(results.elements.size).isEqualTo(1)
        assertThat(results.elements[0].id).isEqualTo("1")
    }

    @Test
    fun `creates a new index and removes the outdated one`() {
        indexWriter.safeRebuildIndex(
            sequenceOf(
                SearchableChannelMetadataFactory.create(
                    id = "1",
                    name = "Beautiful Boy Dancing"
                )
            )
        )

        assertThat(
            indexReader.getSuggestions(
                SuggestionRequest(
                    query = SuggestionQuery(
                        "boy",
                        SuggestionAccessRuleQuery(includedChannelIds = setOf("1"))
                    )
                )
            ).elements
        ).isNotEmpty

        indexWriter.safeRebuildIndex(emptySequence())

        assertThat(
            indexReader.getSuggestions(
                SuggestionRequest(
                    query = SuggestionQuery(
                        "boy",
                        SuggestionAccessRuleQuery(includedChannelIds = setOf("1"))
                    )
                )
            ).elements.isEmpty()
        )
    }
}
