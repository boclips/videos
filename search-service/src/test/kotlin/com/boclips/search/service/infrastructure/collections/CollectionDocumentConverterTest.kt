package com.boclips.search.service.infrastructure.collections

import com.boclips.search.service.domain.collections.model.CollectionVisibility
import com.boclips.search.service.testsupport.SearchableCollectionMetadataFactory
import org.assertj.core.api.Assertions.assertThat
import org.elasticsearch.common.bytes.BytesArray
import org.elasticsearch.search.SearchHit
import org.junit.jupiter.api.Test

class CollectionDocumentConverterTest {

    private val elasticSearchResultConverter =
        CollectionDocumentConverter()

    @Test
    fun `convert search hit`() {
        val searchHit = SearchHit(14).sourceRef(
            BytesArray(
                """
            {
                "id": "14",
                "title": "The title",
                "visibility": "public",
                "subjects": ["crispity", "crunchy"],
                "owner": "juan",
                "description": "Collection under test",
                "hasLessonPlans": "false",
                "ageRange": []
            }
        """.trimIndent()
            )
        )

        val collection = elasticSearchResultConverter.convert(searchHit)

        assertThat(collection).isEqualTo(
            CollectionDocument(
                id = "14",
                title = "The title",
                visibility = "public",
                subjects = listOf("crispity", "crunchy"),
                hasAttachments = false,
                owner = "juan",
                description = "Collection under test",
                hasLessonPlans = false,
                ageRangeMin = null,
                ageRangeMax = null,
                ageRange = emptyList()
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
                "visibility": "public",
                "subjects": ["crispity", "crunchy"],
                "owner": "juan",
                "description": "Collection under test",
                "ageRange": []
            }
        """.trimIndent()
            )
        )

        val collection = elasticSearchResultConverter.convert(searchHit)

        assertThat(collection).isEqualTo(
            CollectionDocument(
                id = "14",
                title = "The title",
                visibility = "public",
                subjects = listOf("crispity", "crunchy"),
                hasAttachments = false,
                owner = "juan",
                description = "Collection under test",
                hasLessonPlans = null,
                ageRangeMin = null,
                ageRangeMax = null,
                ageRange = emptyList()
            )
        )
    }

    @Test
    fun `convert metadata to document`() {
        val metadata = SearchableCollectionMetadataFactory.create(
            id = "14",
            title = "The title",
            visibility = CollectionVisibility.PUBLIC,
            subjects = listOf("crispity", "crunchy"),
            hasAttachments = false,
            owner = "juan",
            description = "Collection under test",
            hasLessonPlans = null,
            ageRangeMin = null,
            ageRangeMax = null,
            bookmarkedBy = setOf("juan")
        )

        val document = CollectionDocumentConverter().convertToDocument(metadata)

        assertThat(document).isEqualTo(
            CollectionDocument(
                id = "14",
                title = "The title",
                visibility = "public",
                subjects = listOf("crispity", "crunchy"),
                bookmarkedBy = setOf("juan"),
                hasAttachments = false,
                owner = "juan",
                description = "Collection under test",
                hasLessonPlans = null,
                ageRangeMin = null,
                ageRangeMax = null,
                ageRange = emptyList()
            )
        )
    }
}
