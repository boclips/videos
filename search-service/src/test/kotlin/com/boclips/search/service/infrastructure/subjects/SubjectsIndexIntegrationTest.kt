package com.boclips.search.service.infrastructure.subjects

import com.boclips.search.service.domain.channels.model.SuggestionQuery
import com.boclips.search.service.domain.common.model.SuggestionRequest
import com.boclips.search.service.domain.subjects.model.SubjectMetadata
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableSubjectMetadataFactory
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SubjectsIndexIntegrationTest : EmbeddedElasticSearchIntegrationTest() {
    lateinit var indexReader: SubjectsIndexReader
    lateinit var indexWriter: SubjectsIndexWriter

    @BeforeEach
    fun setUp() {
        indexReader = SubjectsIndexReader(esClient)
        indexWriter = SubjectsIndexWriter.createTestInstance(esClient, 20)
    }

    @Test
    fun `creates a new index and upserts the subject provided`() {
        indexWriter.safeRebuildIndex(
            sequenceOf(SearchableSubjectMetadataFactory.create(id = "1", name = "Super Subject"))
        )

        val results = indexReader.getSuggestions(
            SuggestionRequest(
                query = SuggestionQuery<SubjectMetadata>(
                    "super"
                )
            )
        )

        Assertions.assertThat(results.elements.size).isEqualTo(1)
        Assertions.assertThat(results.elements[0].id).isEqualTo("1")
    }

    @Test
    fun `returns subject suggestions with 1 character`() {
        indexWriter.safeRebuildIndex(
            sequenceOf(
                SearchableSubjectMetadataFactory.create(id = "1", name = "Super Subject 1"),
                SearchableSubjectMetadataFactory.create(id = "2", name = "Super Subject 2"),
                SearchableSubjectMetadataFactory.create(id = "3", name = "Super Subject 3"),
                SearchableSubjectMetadataFactory.create(id = "4", name = "Super Subject 4"),
                SearchableSubjectMetadataFactory.create(id = "5", name = "Another Subject 5"),
            )
        )

        val results = indexReader.getSuggestions(
            SuggestionRequest(
                query = SuggestionQuery<SubjectMetadata>(
                    "1"
                )
            )
        )

        Assertions.assertThat(results.elements.size).isEqualTo(1)
        Assertions.assertThat(results.elements[0].id).isEqualTo("1")
    }

    @Test
    fun `returns ngram subjects suggestions with 3 characters`() {
        indexWriter.safeRebuildIndex(
            sequenceOf(
                SearchableSubjectMetadataFactory.create(id = "1", name = "Super Subject 1"),
                SearchableSubjectMetadataFactory.create(id = "2", name = "Super Subject 2"),
                SearchableSubjectMetadataFactory.create(id = "3", name = "Super Subject 3"),
                SearchableSubjectMetadataFactory.create(id = "4", name = "Super Subject 4"),
                SearchableSubjectMetadataFactory.create(id = "5", name = "Bad Subject"),
            )
        )

        val results = indexReader.getSuggestions(
            SuggestionRequest(
                query = SuggestionQuery<SubjectMetadata>(
                    "upe"
                )
            )
        )

        Assertions.assertThat(results.elements.size).isEqualTo(4)
    }

    @Test
    fun `returns subject suggestions with 5 characters`() {
        indexWriter.safeRebuildIndex(
            sequenceOf(
                SearchableSubjectMetadataFactory.create(id = "1", name = "Super Subject 1"),
                SearchableSubjectMetadataFactory.create(id = "2", name = "Super Subject 2"),
                SearchableSubjectMetadataFactory.create(id = "3", name = "Super Subject 3"),
                SearchableSubjectMetadataFactory.create(id = "4", name = "Super Subject 4"),
                SearchableSubjectMetadataFactory.create(id = "5", name = "Another Subject 5"),
            )
        )

        val results = indexReader.getSuggestions(
            SuggestionRequest(
                query = SuggestionQuery<SubjectMetadata>(
                    "subje"
                )
            )
        )

        Assertions.assertThat(results.elements.size).isEqualTo(5)
    }

    @Test
    fun `returns subjects suggestions with 1 minute in a museum channel`() {
        indexWriter.safeRebuildIndex(
            sequenceOf(
                SearchableSubjectMetadataFactory.create(id = "1", name = "1 Minute in a Museum"),
                SearchableSubjectMetadataFactory.create(id = "2", name = "AP"),
                SearchableSubjectMetadataFactory.create(id = "3", name = "AllTime 10s"),
                SearchableSubjectMetadataFactory.create(id = "4", name = "AFPTV"),
                SearchableSubjectMetadataFactory.create(id = "5", name = "360 Cities"),
                SearchableSubjectMetadataFactory.create(id = "6", name = "TED"),
                SearchableSubjectMetadataFactory.create(id = "7", name = "TED-X"),
                SearchableSubjectMetadataFactory.create(id = "8", name = "Crash Course Engineering"),
                SearchableSubjectMetadataFactory.create(id = "9", name = "Crash Course"),
                SearchableSubjectMetadataFactory.create(id = "10", name = "Crash Course Physics"),
            )
        )

        val results = indexReader.getSuggestions(
            SuggestionRequest(
                query = SuggestionQuery<SubjectMetadata>(
                    "1 Minute"
                )
            )
        )

        Assertions.assertThat(results.elements.size).isEqualTo(1)
        Assertions.assertThat(results.elements[0].id).isEqualTo("1")
    }

    @Test
    fun `returns subject suggestions with ted subjects`() {
        indexWriter.safeRebuildIndex(
            sequenceOf(
                SearchableSubjectMetadataFactory.create(id = "1", name = "1 Minute in a Museum"),
                SearchableSubjectMetadataFactory.create(id = "2", name = "AP"),
                SearchableSubjectMetadataFactory.create(id = "3", name = "AllTime 10s"),
                SearchableSubjectMetadataFactory.create(id = "4", name = "AFPTV"),
                SearchableSubjectMetadataFactory.create(id = "5", name = "360 Cities"),
                SearchableSubjectMetadataFactory.create(id = "6", name = "TED"),
                SearchableSubjectMetadataFactory.create(id = "7", name = "TED-X"),
                SearchableSubjectMetadataFactory.create(id = "8", name = "Crash Course Engineering"),
                SearchableSubjectMetadataFactory.create(id = "9", name = "Crash Course"),
                SearchableSubjectMetadataFactory.create(id = "10", name = "Crash Course Physics"),
            )
        )

        val results = indexReader.getSuggestions(
            SuggestionRequest(
                query = SuggestionQuery<SubjectMetadata>(
                    "ted"
                )
            )
        )

        Assertions.assertThat(results.elements.size).isEqualTo(2)
        Assertions.assertThat(results.elements[0].id).isEqualTo("6")
        Assertions.assertThat(results.elements[1].id).isEqualTo("7")
    }

    @Test
    fun `returns subject suggestions with crash course channels`() {
        indexWriter.safeRebuildIndex(
            sequenceOf(
                SearchableSubjectMetadataFactory.create(id = "1", name = "1 Minute in a Museum"),
                SearchableSubjectMetadataFactory.create(id = "2", name = "AP"),
                SearchableSubjectMetadataFactory.create(id = "3", name = "AllTime 10s"),
                SearchableSubjectMetadataFactory.create(id = "4", name = "AFPTV"),
                SearchableSubjectMetadataFactory.create(id = "5", name = "360 Cities"),
                SearchableSubjectMetadataFactory.create(id = "6", name = "TED"),
                SearchableSubjectMetadataFactory.create(id = "7", name = "TED-X"),
                SearchableSubjectMetadataFactory.create(id = "8", name = "Crash Course Engineering"),
                SearchableSubjectMetadataFactory.create(id = "9", name = "Crash Course"),
                SearchableSubjectMetadataFactory.create(id = "10", name = "Crash Course Physics"),
            )
        )

        val results = indexReader.getSuggestions(
            SuggestionRequest(
                query = SuggestionQuery<SubjectMetadata>(
                    "Crash"
                )
            )
        )

        Assertions.assertThat(results.elements.size).isEqualTo(3)
        Assertions.assertThat(results.elements[0].id).isEqualTo("9") // elements[0] = Crash Course
    }

    @Test
    fun `returns subject suggestions with crash course engineering channels`() {
        indexWriter.safeRebuildIndex(
            sequenceOf(
                SearchableSubjectMetadataFactory.create(id = "1", name = "1 Minute in a Museum"),
                SearchableSubjectMetadataFactory.create(id = "2", name = "AP"),
                SearchableSubjectMetadataFactory.create(id = "3", name = "AllTime 10s"),
                SearchableSubjectMetadataFactory.create(id = "4", name = "AFPTV"),
                SearchableSubjectMetadataFactory.create(id = "5", name = "360 Cities"),
                SearchableSubjectMetadataFactory.create(id = "6", name = "TED"),
                SearchableSubjectMetadataFactory.create(id = "7", name = "TED-X"),
                SearchableSubjectMetadataFactory.create(id = "8", name = "Crash Course Engineering"),
                SearchableSubjectMetadataFactory.create(id = "9", name = "Crash Course"),
                SearchableSubjectMetadataFactory.create(id = "10", name = "Crash Course Physics"),
            )
        )

        val results = indexReader.getSuggestions(
            SuggestionRequest(
                query = SuggestionQuery<SubjectMetadata>(
                    "ering"
                )
            )
        )

        Assertions.assertThat(results.elements.size).isEqualTo(1)
        Assertions.assertThat(results.elements[0].id).isEqualTo("8")
    }

    @Test
    fun `returns subject suggestions with just one character`() {
        indexWriter.safeRebuildIndex(
            sequenceOf(
                SearchableSubjectMetadataFactory.create(id = "1", name = "1 Minute in a Museum"),
                SearchableSubjectMetadataFactory.create(id = "2", name = "AP"),
                SearchableSubjectMetadataFactory.create(id = "3", name = "AllTime 10s"),
                SearchableSubjectMetadataFactory.create(id = "4", name = "AFPTV"),
                SearchableSubjectMetadataFactory.create(id = "5", name = "360 Cities"),
                SearchableSubjectMetadataFactory.create(id = "6", name = "TED"),
                SearchableSubjectMetadataFactory.create(id = "7", name = "TED-X"),
                SearchableSubjectMetadataFactory.create(id = "8", name = "Crash Course Engineering"),
                SearchableSubjectMetadataFactory.create(id = "9", name = "Crash Course"),
                SearchableSubjectMetadataFactory.create(id = "10", name = "Crash Course Physics"),
            )
        )

        val results = indexReader.getSuggestions(
            SuggestionRequest(
                query = SuggestionQuery<SubjectMetadata>(
                    "3"
                )
            )
        )

        Assertions.assertThat(results.elements.size).isEqualTo(1)
        Assertions.assertThat(results.elements[0].id).isEqualTo("5")
    }

    @Test
    fun `upserts subject to index`() {
        indexWriter.upsert(
            sequenceOf(
                SearchableSubjectMetadataFactory.create(
                    id = "1",
                    name = "Beautiful Boy Dancing"
                )
            )
        )

        val results = indexReader.getSuggestions(
            SuggestionRequest(
                query = SuggestionQuery<SubjectMetadata>("Boy")
            )
        )

        Assertions.assertThat(results.elements.size).isEqualTo(1)
        Assertions.assertThat(results.elements[0].id).isEqualTo("1")
    }

    @Test
    fun `creates a new index and removes the outdated one`() {
        indexWriter.safeRebuildIndex(
            sequenceOf(
                SearchableSubjectMetadataFactory.create(
                    id = "1",
                    name = "Beautiful Boy Dancing"
                )
            )
        )

        Assertions.assertThat(
            indexReader.getSuggestions(
                SuggestionRequest(
                    query = SuggestionQuery<SubjectMetadata>(
                        "boy"
                    )
                )
            ).elements
        ).isNotEmpty

        indexWriter.safeRebuildIndex(emptySequence())

        Assertions.assertThat(
            indexReader.getSuggestions(
                SuggestionRequest(
                    query = SuggestionQuery<SubjectMetadata>(
                        "boy"
                    )
                )
            ).elements.isEmpty()
        )
    }
}
