package com.boclips.videos.service.infrastructure

import com.boclips.videos.service.infrastructure.search.ElasticSearchVideo
import com.boclips.videos.service.infrastructure.search.SearchHitConverter
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.elasticsearch.common.bytes.BytesArray
import org.elasticsearch.search.SearchHit
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired

class SearchHitConverterTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var searchHitConverter: SearchHitConverter

    @Test
    fun convert() {

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

        val video = searchHitConverter.convert(searchHit)

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