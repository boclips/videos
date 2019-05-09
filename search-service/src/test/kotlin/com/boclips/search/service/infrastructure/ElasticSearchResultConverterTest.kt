package com.boclips.search.service.infrastructure

import org.assertj.core.api.Assertions.assertThat
import org.elasticsearch.common.bytes.BytesArray
import org.elasticsearch.search.SearchHit
import org.junit.jupiter.api.Test

class ElasticSearchResultConverterTest {

    private val elasticSearchResultConverter = ElasticSearchResultConverter()

    @Test
    fun `convert search hit`() {
        val searchHit = SearchHit(14).sourceRef(
            BytesArray(
                """
            {
                "id": "14",
                "title": "The title",
                "description": "The description",
                "contentProvider": "TED Talks",
                "price_category": "expensive",
                "duration": "02:01:20",
                "keywords": ["k1","k2"],
                "tags": ["news", "classroom"],
                "durationSeconds": 10,
                "source": "Boclips"
            }
        """.trimIndent()
            )
        )

        val video = elasticSearchResultConverter.convert(searchHit)

        assertThat(video).isEqualTo(
            ElasticSearchVideo(
                id = "14",
                title = "The title",
                description = "The description",
                contentProvider = "TED Talks",
                releaseDate = null,
                keywords = listOf("k1", "k2"),
                tags = listOf("news", "classroom"),
                durationSeconds = 10,
                source = "Boclips"
            )
        )
    }
}
