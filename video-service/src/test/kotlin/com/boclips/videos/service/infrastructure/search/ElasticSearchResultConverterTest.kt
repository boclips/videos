package com.boclips.videos.service.infrastructure.search

import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.elasticsearch.common.bytes.BytesArray
import org.elasticsearch.search.SearchHit
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired

class ElasticSearchResultConverterTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var elasticSearchResultConverter: ElasticSearchResultConverter

    @Test
    fun `convert search hit`() {
        val searchHit = SearchHit(14).sourceRef(BytesArray("""
            {
                "id": "14",
                "reference_id": "ref-id-14",
                "title": "The title",
                "description": "The description",
                "source": "TeD",
                "price_category": "expensive",
                "date": "2014-05-13",
                "duration": "02:01:20"
            }
        """.trimIndent()))

        val video = elasticSearchResultConverter.convert(searchHit)

        assertThat(video).isEqualTo(ElasticSearchVideo(
                id = "14",
                referenceId = "ref-id-14",
                title = "The title",
                description = "The description",
                source = "TeD",
                date = "2014-05-13"
        ))
    }

}