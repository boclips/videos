package com.boclips.search.service.infrastructure.collections

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
                "hasLessonPlans": "false"
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
                ageRangeMax = null
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
                "description": "Collection under test"
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
                ageRangeMax = null
            )
        )
    }
}
