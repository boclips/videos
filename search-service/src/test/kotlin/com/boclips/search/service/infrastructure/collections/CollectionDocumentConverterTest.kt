package com.boclips.search.service.infrastructure.collections

import com.boclips.search.service.testsupport.SearchableCollectionMetadataFactory
import org.assertj.core.api.Assertions.assertThat
import org.elasticsearch.common.bytes.BytesArray
import org.elasticsearch.search.SearchHit
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.Month

class CollectionDocumentConverterTest {

    private val elasticSearchResultConverter = CollectionDocumentConverter()

    @Test
    fun `convert search hit`() {
        val searchHit = SearchHit(14).sourceRef(
            BytesArray(
                """
            {
                "id": "14",
                "title": "The title",
                "subjects": ["crispity", "crunchy"],
                "owner": "juan",
                "description": "Collection under test",
                "hasLessonPlans": "false",
                "ageRange": [],
                "updatedAt": "2019-01-16T12:00:00.870Z",
                "attachmentTypes": ["Lesson Guide"]
            }
        """.trimIndent()
            )
        )

        val collection = elasticSearchResultConverter.convert(searchHit)

        assertThat(collection).isEqualTo(
            CollectionDocument(
                id = "14",
                title = "The title",
                searchable = false,
                subjects = listOf("crispity", "crunchy"),
                hasAttachments = false,
                owner = "juan",
                description = "Collection under test",
                hasLessonPlans = false,
                promoted = null,
                ageRangeMin = null,
                ageRangeMax = null,
                ageRange = emptyList(),
                updatedAt = LocalDate.of(2019, Month.JANUARY, 16),
                attachmentTypes = setOf("Lesson Guide")
            )
        )
    }

    @Test
    fun `convert search hit without lesson plans`() {
        val searchHit = SearchHit(14).sourceRef(
            BytesArray(
                """
            {
                "id": "14",
                "title": "The title",
                "subjects": ["crispity", "crunchy"],
                "owner": "juan",
                "description": "Collection under test",
                "ageRange": [],
                "updatedAt": "2018-12-19T00:00:00Z"
            }
        """.trimIndent()
            )
        )

        val collection = elasticSearchResultConverter.convert(searchHit)

        assertThat(collection).isEqualTo(
            CollectionDocument(
                id = "14",
                title = "The title",
                searchable = false,
                subjects = listOf("crispity", "crunchy"),
                hasAttachments = false,
                owner = "juan",
                description = "Collection under test",
                hasLessonPlans = null,
                promoted = null,
                ageRangeMin = null,
                ageRangeMax = null,
                ageRange = emptyList(),
                updatedAt = LocalDate.of(2018, Month.DECEMBER, 19),
                attachmentTypes = null
            )
        )
    }

    @Test
    fun `convert metadata to document`() {
        val metadata = SearchableCollectionMetadataFactory.create(
            id = "14",
            title = "The title",
            searchable = true,
            subjects = listOf("crispity", "crunchy"),
            hasAttachments = false,
            owner = "juan",
            description = "Collection under test",
            hasLessonPlans = null,
            promoted = true,
            ageRangeMin = null,
            ageRangeMax = null,
            bookmarkedBy = setOf("juan"),
            updatedAt = LocalDate.of(2000, Month.APRIL, 12),
            attachmentTypes = setOf("Activity")
        )

        val document = CollectionDocumentConverter().convertToDocument(metadata)

        assertThat(document).isEqualTo(
            CollectionDocument(
                id = "14",
                title = "The title",
                searchable = true,
                subjects = listOf("crispity", "crunchy"),
                bookmarkedBy = setOf("juan"),
                hasAttachments = false,
                owner = "juan",
                description = "Collection under test",
                hasLessonPlans = null,
                promoted = true,
                ageRangeMin = null,
                ageRangeMax = null,
                ageRange = emptyList(),
                updatedAt = LocalDate.of(2000, Month.APRIL, 12),
                attachmentTypes = setOf("Activity")
            )
        )
    }
}
