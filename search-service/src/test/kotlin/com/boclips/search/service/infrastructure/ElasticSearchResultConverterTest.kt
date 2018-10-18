package com.boclips.search.service.infrastructure

import org.assertj.core.api.Assertions.assertThat
import org.elasticsearch.common.bytes.BytesArray
import org.elasticsearch.search.SearchHit
import org.junit.jupiter.api.Test

class ElasticSearchResultConverterTest {

    private val elasticSearchResultConverter = ElasticSearchResultConverter()

    @Test
    fun `convert search hit`() {
        val searchHit = SearchHit(14).sourceRef(BytesArray("""
            {
                "id": "14",
                "title": "The title",
                "description": "The description",
                "source": "TeD",
                "price_category": "expensive",
                "date": "2014-05-13",
                "duration": "02:01:20",
                "keywords": ["k1","k2"]
            }
        """.trimIndent()))

        val video = elasticSearchResultConverter.convert(searchHit)

        assertThat(video).isEqualTo(ElasticSearchVideo(
                id = "14",
                title = "The title",
                description = "The description",
                keywords = listOf("k1", "k2")
        ))
    }

}
