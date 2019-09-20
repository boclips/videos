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
                "owner": "juan"
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
                owner = "juan"
            )
        )
    }
}
