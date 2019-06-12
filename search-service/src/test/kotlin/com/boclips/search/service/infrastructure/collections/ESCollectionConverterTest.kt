package com.boclips.search.service.infrastructure.collections

import org.assertj.core.api.Assertions.assertThat
import org.elasticsearch.common.bytes.BytesArray
import org.elasticsearch.search.SearchHit
import org.junit.jupiter.api.Test

class ESCollectionConverterTest {

    private val elasticSearchResultConverter =
        ESCollectionConverter()

    @Test
    fun `convert search hit`() {
        val searchHit = SearchHit(14).sourceRef(
            BytesArray(
                """
            {
                "id": "14",
                "title": "The title",
                "subjects": ["crispity", "crunchy"]
            }
        """.trimIndent()
            )
        )

        val collection = elasticSearchResultConverter.convert(searchHit)

        assertThat(collection).isEqualTo(
            ESCollection(
                id = "14",
                title = "The title",
                subjects = listOf("crispity", "crunchy")
            )
        )
    }
}
