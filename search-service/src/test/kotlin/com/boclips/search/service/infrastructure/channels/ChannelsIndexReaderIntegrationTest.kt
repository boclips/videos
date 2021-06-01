package com.boclips.search.service.infrastructure.channels

import com.boclips.search.service.domain.channels.model.ChannelQuery
import com.boclips.search.service.domain.channels.model.ContentType
import com.boclips.search.service.domain.channels.model.SuggestionAccessRuleQuery
import com.boclips.search.service.domain.channels.model.SuggestionQuery
import com.boclips.search.service.domain.common.model.PaginatedIndexSearchRequest
import com.boclips.search.service.domain.common.model.SuggestionRequest
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableChannelMetadataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ChannelsIndexReaderIntegrationTest : EmbeddedElasticSearchIntegrationTest() {
    lateinit var indexReader: ChannelsIndexReader
    lateinit var indexWriter: ChannelsIndexWriter

    @BeforeEach
    fun setUp() {
        indexReader = ChannelsIndexReader(esClient)
        indexWriter = ChannelsIndexWriter.createTestInstance(esClient, 20)
    }

    @Test
    fun `returns channel suggestions with 1 character`() {
        indexWriter.safeRebuildIndex(
            sequenceOf(
                SearchableChannelMetadataFactory.create(id = "1", name = "Super Channel 1"),
                SearchableChannelMetadataFactory.create(id = "2", name = "Super Channel 2"),
                SearchableChannelMetadataFactory.create(id = "3", name = "Super Channel 3"),
                SearchableChannelMetadataFactory.create(id = "4", name = "Super Channel 4"),
                SearchableChannelMetadataFactory.create(id = "5", name = "Another Channel 5"),
            )
        )

        val results = indexReader.getSuggestions(
            SuggestionRequest(
                query = SuggestionQuery(
                    "1",
                    SuggestionAccessRuleQuery(includedChannelIds = setOf("1", "2", "3", "4", "5"))
                )
            )
        )

        assertThat(results.elements.size).isEqualTo(1)
        assertThat(results.elements[0].id).isEqualTo("1")
    }

    @Test
    fun `returns ngram channels suggestions with 3 characters`() {
        indexWriter.safeRebuildIndex(
            sequenceOf(
                SearchableChannelMetadataFactory.create(id = "1", name = "Super Channel 1"),
                SearchableChannelMetadataFactory.create(id = "2", name = "Super Channel 2"),
                SearchableChannelMetadataFactory.create(id = "3", name = "Super Channel 3"),
                SearchableChannelMetadataFactory.create(id = "4", name = "Super Channel 4"),
                SearchableChannelMetadataFactory.create(id = "5", name = "Bad Channel"),
            )
        )

        val results = indexReader.getSuggestions(
            SuggestionRequest(
                query = SuggestionQuery(
                    "chan",
                    SuggestionAccessRuleQuery(
                        includedChannelIds = setOf("1", "2", "3", "4"),
                        excludedContentPartnerIds = setOf("5")
                    )
                )
            )
        )

        assertThat(results.elements.size).isEqualTo(4)
    }

    @Test
    fun `returns channel suggestions with 5 characters`() {
        indexWriter.safeRebuildIndex(
            sequenceOf(
                SearchableChannelMetadataFactory.create(id = "1", name = "Super Channel 1"),
                SearchableChannelMetadataFactory.create(id = "2", name = "Super Channel 2"),
                SearchableChannelMetadataFactory.create(id = "3", name = "Super Channel 3"),
                SearchableChannelMetadataFactory.create(id = "4", name = "Super Channel 4"),
                SearchableChannelMetadataFactory.create(id = "5", name = "Another Channel 5"),
            )
        )

        val results = indexReader.getSuggestions(
            SuggestionRequest(
                query = SuggestionQuery(
                    "annel",
                    SuggestionAccessRuleQuery(includedChannelIds = setOf("1", "2", "3", "4", "5"))
                )
            )
        )

        assertThat(results.elements.size).isEqualTo(5)
    }

    @Test
    fun `returns channel suggestions with 1 minute in a museum channel`() {
        indexWriter.safeRebuildIndex(
            sequenceOf(
                SearchableChannelMetadataFactory.create(id = "1", name = "1 Minute in a Museum"),
                SearchableChannelMetadataFactory.create(id = "2", name = "AP"),
                SearchableChannelMetadataFactory.create(id = "3", name = "AllTime 10s"),
                SearchableChannelMetadataFactory.create(id = "4", name = "AFPTV"),
                SearchableChannelMetadataFactory.create(id = "5", name = "360 Cities"),
                SearchableChannelMetadataFactory.create(id = "6", name = "TED"),
                SearchableChannelMetadataFactory.create(id = "7", name = "TED-X"),
                SearchableChannelMetadataFactory.create(id = "8", name = "Crash Course Engineering"),
                SearchableChannelMetadataFactory.create(id = "9", name = "Crash Course"),
                SearchableChannelMetadataFactory.create(id = "10", name = "Crash Course Physics"),
            )
        )

        val results = indexReader.getSuggestions(
            SuggestionRequest(
                query = SuggestionQuery(
                    "1 Minute",
                    SuggestionAccessRuleQuery(
                        includedChannelIds = setOf(
                            "1",
                            "2",
                            "3",
                            "4",
                            "5",
                            "6",
                            "7",
                            "8",
                            "9",
                            "10"
                        )
                    )
                )
            )
        )

        assertThat(results.elements.size).isEqualTo(1)
        assertThat(results.elements[0].id).isEqualTo("1")
    }

    @Test
    fun `returns channel suggestions with ted channels`() {
        indexWriter.safeRebuildIndex(
            sequenceOf(
                SearchableChannelMetadataFactory.create(id = "1", name = "1 Minute in a Museum"),
                SearchableChannelMetadataFactory.create(id = "2", name = "AP"),
                SearchableChannelMetadataFactory.create(id = "3", name = "AllTime 10s"),
                SearchableChannelMetadataFactory.create(id = "4", name = "AFPTV"),
                SearchableChannelMetadataFactory.create(id = "5", name = "360 Cities"),
                SearchableChannelMetadataFactory.create(id = "6", name = "TED"),
                SearchableChannelMetadataFactory.create(id = "7", name = "TED-X"),
                SearchableChannelMetadataFactory.create(id = "8", name = "Crash Course Engineering"),
                SearchableChannelMetadataFactory.create(id = "9", name = "Crash Course"),
                SearchableChannelMetadataFactory.create(id = "10", name = "Crash Course Physics"),
            )
        )

        val results = indexReader.getSuggestions(
            SuggestionRequest(
                query = SuggestionQuery(
                    "ted",
                    SuggestionAccessRuleQuery(
                        includedChannelIds = setOf(
                            "1",
                            "2",
                            "3",
                            "4",
                            "5",
                            "6",
                            "7",
                            "8",
                            "9",
                            "10"
                        )
                    )
                )
            )
        )

        assertThat(results.elements.size).isEqualTo(2)
        assertThat(results.elements[0].id).isEqualTo("6")
        assertThat(results.elements[1].id).isEqualTo("7")
    }

    @Test
    fun `returns channel suggestions with whitespace in query`() {
        indexWriter.safeRebuildIndex(
            sequenceOf(
                SearchableChannelMetadataFactory.create(id = "1", name = "1 Minute in a Museum"),
                SearchableChannelMetadataFactory.create(id = "2", name = "AP"),
                SearchableChannelMetadataFactory.create(id = "3", name = "AllTime 10s"),
                SearchableChannelMetadataFactory.create(id = "4", name = "AFPTV"),
                SearchableChannelMetadataFactory.create(id = "5", name = "360 Cities"),
                SearchableChannelMetadataFactory.create(id = "6", name = "TED"),
                SearchableChannelMetadataFactory.create(id = "7", name = "TED-X"),
                SearchableChannelMetadataFactory.create(id = "8", name = "Crash Course Engineering"),
                SearchableChannelMetadataFactory.create(id = "9", name = "Crash Course"),
                SearchableChannelMetadataFactory.create(id = "10", name = "Crash Course Physics"),
            )
        )

        val results = indexReader.getSuggestions(
            SuggestionRequest(
                query = SuggestionQuery(
                    "crash co",
                    SuggestionAccessRuleQuery(
                        includedChannelIds = setOf(
                            "1",
                            "2",
                            "3",
                            "4",
                            "5",
                            "6",
                            "7",
                            "8",
                            "9",
                            "10"
                        )
                    )
                )
            )
        )

        assertThat(results.elements.size).isEqualTo(3)
        assertThat(results.elements[0].id).isEqualTo("9")
        assertThat(results.elements[1].id).isEqualTo("8")
        assertThat(results.elements[2].id).isEqualTo("10")
    }

    @Test
    fun `returns channel suggestions with crash course channels`() {
        indexWriter.safeRebuildIndex(
            sequenceOf(
                SearchableChannelMetadataFactory.create(id = "1", name = "1 Minute in a Museum"),
                SearchableChannelMetadataFactory.create(id = "2", name = "AP"),
                SearchableChannelMetadataFactory.create(id = "3", name = "AllTime 10s"),
                SearchableChannelMetadataFactory.create(id = "4", name = "AFPTV"),
                SearchableChannelMetadataFactory.create(id = "5", name = "360 Cities"),
                SearchableChannelMetadataFactory.create(id = "6", name = "TED"),
                SearchableChannelMetadataFactory.create(id = "7", name = "TED-X"),
                SearchableChannelMetadataFactory.create(id = "8", name = "Crash Course Engineering"),
                SearchableChannelMetadataFactory.create(id = "9", name = "Crash Course"),
                SearchableChannelMetadataFactory.create(id = "10", name = "Crash Course Physics"),
            )
        )

        val results = indexReader.getSuggestions(
            SuggestionRequest(
                query = SuggestionQuery(
                    "Crash",
                    SuggestionAccessRuleQuery(
                        includedChannelIds = setOf(
                            "1",
                            "2",
                            "3",
                            "4",
                            "5",
                            "6",
                            "7",
                            "8",
                            "9",
                            "10"
                        )
                    )
                )
            )
        )

        assertThat(results.elements.size).isEqualTo(3)
        assertThat(results.elements[0].id).isEqualTo("9") // elements[0] = Crash Course
    }

    @Test
    fun `returns channel suggestions with crash course channels with access rules`() {
        indexWriter.safeRebuildIndex(
            sequenceOf(
                SearchableChannelMetadataFactory.create(id = "1", name = "1 Minute in a Museum"),
                SearchableChannelMetadataFactory.create(id = "2", name = "AP"),
                SearchableChannelMetadataFactory.create(id = "3", name = "AllTime 10s"),
                SearchableChannelMetadataFactory.create(id = "4", name = "AFPTV"),
                SearchableChannelMetadataFactory.create(id = "5", name = "360 Cities"),
                SearchableChannelMetadataFactory.create(id = "6", name = "TED"),
                SearchableChannelMetadataFactory.create(id = "7", name = "TED-X"),
                SearchableChannelMetadataFactory.create(id = "8", name = "Crash Course Engineering"),
                SearchableChannelMetadataFactory.create(id = "9", name = "Crash Course"),
                SearchableChannelMetadataFactory.create(id = "10", name = "Crash Course Physics"),
            )
        )

        val results = indexReader.getSuggestions(
            SuggestionRequest(
                query = SuggestionQuery(
                    "crash",
                    SuggestionAccessRuleQuery(
                        includedChannelIds = setOf("1", "2", "3", "4", "5", "6", "7", "8", "9"),
                        excludedContentPartnerIds = setOf("10")
                    )
                )
            )
        )

        assertThat(results.elements.size).isEqualTo(2)
        assertThat(results.elements[0].id).isEqualTo("9") // elements[0] = Crash Course
    }

    @Test
    fun `returns channel suggestions with crash course engineering channels`() {
        indexWriter.safeRebuildIndex(
            sequenceOf(
                SearchableChannelMetadataFactory.create(id = "1", name = "1 Minute in a Museum"),
                SearchableChannelMetadataFactory.create(id = "2", name = "AP"),
                SearchableChannelMetadataFactory.create(id = "3", name = "AllTime 10s"),
                SearchableChannelMetadataFactory.create(id = "4", name = "AFPTV"),
                SearchableChannelMetadataFactory.create(id = "5", name = "360 Cities"),
                SearchableChannelMetadataFactory.create(id = "6", name = "TED"),
                SearchableChannelMetadataFactory.create(id = "7", name = "TED-X"),
                SearchableChannelMetadataFactory.create(id = "8", name = "Crash Course Engineering"),
                SearchableChannelMetadataFactory.create(id = "9", name = "Crash Course"),
                SearchableChannelMetadataFactory.create(id = "10", name = "Crash Course Physics"),
            )
        )

        val results = indexReader.getSuggestions(
            SuggestionRequest(
                query = SuggestionQuery(
                    "ering",
                    SuggestionAccessRuleQuery(
                        includedChannelIds = setOf(
                            "1",
                            "2",
                            "3",
                            "4",
                            "5",
                            "6",
                            "7",
                            "8",
                            "9",
                            "10"
                        )
                    )
                )
            )
        )

        assertThat(results.elements.size).isEqualTo(1)
        assertThat(results.elements[0].id).isEqualTo("8")
    }

    @Test
    fun `returns channel suggestions with just one character`() {
        indexWriter.safeRebuildIndex(
            sequenceOf(
                SearchableChannelMetadataFactory.create(id = "1", name = "1 Minute in a Museum"),
                SearchableChannelMetadataFactory.create(id = "2", name = "AP"),
                SearchableChannelMetadataFactory.create(id = "3", name = "AllTime 10s"),
                SearchableChannelMetadataFactory.create(id = "4", name = "AFPTV"),
                SearchableChannelMetadataFactory.create(id = "5", name = "360 Cities"),
                SearchableChannelMetadataFactory.create(id = "6", name = "TED"),
                SearchableChannelMetadataFactory.create(id = "7", name = "TED-X"),
                SearchableChannelMetadataFactory.create(id = "8", name = "Crash Course Engineering"),
                SearchableChannelMetadataFactory.create(id = "9", name = "Crash Course"),
                SearchableChannelMetadataFactory.create(id = "10", name = "Crash Course Physics"),
            )
        )

        val results = indexReader.getSuggestions(
            SuggestionRequest(
                query = SuggestionQuery(
                    "3",
                    SuggestionAccessRuleQuery(
                        includedChannelIds = setOf(
                            "1",
                            "2",
                            "3",
                            "4",
                            "5",
                            "6",
                            "7",
                            "8",
                            "9",
                            "10"
                        )
                    )
                )
            )
        )

        assertThat(results.elements.size).isEqualTo(1)
        assertThat(results.elements[0].id).isEqualTo("5")
    }

    @Test
    fun `creates a new index and applies eligible for stream filter`() {
        indexWriter.safeRebuildIndex(
            sequenceOf(
                SearchableChannelMetadataFactory.create(id = "1", name = "Super Channel", eligibleForStream = true),
                SearchableChannelMetadataFactory.create(
                    id = "2",
                    name = "Super Mega Channel",
                    eligibleForStream = false
                )
            )
        )

        val results = indexReader.getSuggestions(
            SuggestionRequest(
                query = SuggestionQuery(
                    "super",
                    SuggestionAccessRuleQuery(isEligibleForStream = true)
                )
            )
        )

        assertThat(results.elements.size).isEqualTo(1)
        assertThat(results.elements[0].id).isEqualTo("1")
    }

    @Test
    fun `creates a new index and applies excluded content types filter`() {
        indexWriter.safeRebuildIndex(
            sequenceOf(
                SearchableChannelMetadataFactory.create(
                    id = "1",
                    name = "Super Channel",
                    contentTypes = listOf(ContentType.NEWS)
                ),
                SearchableChannelMetadataFactory.create(
                    id = "2",
                    name = "Super Mega Channel",
                    contentTypes = listOf(ContentType.INSTRUCTIONAL, ContentType.STOCK)
                )
            )
        )

        val results = indexReader.getSuggestions(
            SuggestionRequest(
                query = SuggestionQuery(
                    "super",
                    SuggestionAccessRuleQuery(excludedTypes = setOf(ContentType.STOCK))
                )
            )
        )

        assertThat(results.elements.size).isEqualTo(1)
        assertThat(results.elements[0].id).isEqualTo("1")
    }

    @Test
    fun `creates a new index and applies included content types filter`() {
        indexWriter.safeRebuildIndex(
            sequenceOf(
                SearchableChannelMetadataFactory.create(
                    id = "1",
                    name = "Super Channel",
                    contentTypes = listOf(ContentType.NEWS)
                ),
                SearchableChannelMetadataFactory.create(
                    id = "2",
                    name = "Super Mega Channel",
                    contentTypes = listOf(ContentType.INSTRUCTIONAL, ContentType.STOCK)
                )
            )
        )

        val results = indexReader.getSuggestions(
            SuggestionRequest(
                query = SuggestionQuery(
                    "super",
                    SuggestionAccessRuleQuery(includedTypes = setOf(ContentType.STOCK))
                )
            )
        )

        assertThat(results.elements.size).isEqualTo(1)
        assertThat(results.elements[0].id).isEqualTo("2")
    }

    @Test
    fun `upserts channel to index`() {
        indexWriter.upsert(
            sequenceOf(
                SearchableChannelMetadataFactory.create(
                    id = "1",
                    name = "Beautiful Boy Dancing"
                )
            )
        )

        val results = indexReader.getSuggestions(
            SuggestionRequest(
                query = SuggestionQuery("Boy", SuggestionAccessRuleQuery(includedChannelIds = setOf("1")))
            )
        )

        assertThat(results.elements.size).isEqualTo(1)
        assertThat(results.elements[0].id).isEqualTo("1")
    }

    @Test
    fun `default sort by name is case insensitive`() {
        indexWriter.upsert(
            sequenceOf(
                SearchableChannelMetadataFactory.create(
                    id = "1",
                    name = "BB"
                ),
                SearchableChannelMetadataFactory.create(
                    id = "2",
                    name = "aa"
                ),
                SearchableChannelMetadataFactory.create(
                    id = "3",
                    name = "AA channel"
                ),
            )
        )

        val results = indexReader.search(PaginatedIndexSearchRequest(query = ChannelQuery()))

        assertThat(results.elements).containsExactly("2", "3", "1")
    }
}
