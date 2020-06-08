package com.boclips.search.service.infrastructure.collections

import com.boclips.search.service.testsupport.SearchableCollectionMetadataFactory
import org.assertj.core.api.Assertions.assertThat
import org.elasticsearch.common.bytes.BytesArray
import org.elasticsearch.search.SearchHit
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime

class CollectionDocumentConverterTest {
    private val elasticSearchResultConverter = CollectionDocumentConverter()
    private val aVerySpecialDateTimeSerialised = "2017-04-24T09:30Z[UTC]"
    private val aVerySpecialDateTime = ZonedDateTime.parse(aVerySpecialDateTimeSerialised)

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
                "lastModified": "$aVerySpecialDateTimeSerialised",
                "attachmentTypes": ["Lesson Guide"],
                "default": true
            }
        """.trimIndent()
            )
        )

        val actualDocument = elasticSearchResultConverter.convert(searchHit)

        val expectedDocument = CollectionDocument(
            id = "14",
            title = "The title",
            searchable = false,
            subjects = listOf("crispity", "crunchy"),
            hasAttachments = false,
            owner = "juan",
            description = "Collection under test",
            hasLessonPlans = false,
            promoted = false,
            ageRangeMin = null,
            ageRangeMax = null,
            ageRange = emptyList(),
            lastModified = aVerySpecialDateTime,
            attachmentTypes = setOf("Lesson Guide"),
            default = true
        )

        assertThat(actualDocument).isEqualTo(expectedDocument)
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
                "lastModified": "$aVerySpecialDateTimeSerialised"
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
                promoted = false,
                ageRangeMin = null,
                ageRangeMax = null,
                ageRange = emptyList(),
                lastModified = aVerySpecialDateTime,
                attachmentTypes = null,
                default = false
            )
        )
    }

    @Test
    fun `convert metadata to document`() {
        val metadata = SearchableCollectionMetadataFactory.create(
            id = "14",
            title = "The title",
            subjects = listOf("crispity", "crunchy"),
            hasAttachments = false,
            owner = "juan",
            bookmarkedBy = setOf("juan"),
            description = "Collection under test",
            hasLessonPlans = null,
            searchable = true,
            promoted = true,
            ageRangeMin = null,
            ageRangeMax = null,
            lastModified = aVerySpecialDateTime,
            attachmentTypes = setOf("Activity"),
            default = true
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
                lastModified = aVerySpecialDateTime,
                attachmentTypes = setOf("Activity"),
                default = true
            )
        )
    }
}
