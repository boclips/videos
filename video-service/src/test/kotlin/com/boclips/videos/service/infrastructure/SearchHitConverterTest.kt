package com.boclips.videos.service.infrastructure

import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.infrastructure.search.SearchHitConverter
import org.assertj.core.api.Assertions.assertThat
import org.elasticsearch.common.bytes.BytesArray
import org.elasticsearch.search.SearchHit
import org.junit.Test

class SearchHitConverterTest {

    @Test
    fun convert() {

        val searchHit = SearchHit(14).sourceRef(BytesArray("""
            {
                "id": "14",
                "title": "The title",
                "description": "The description",
                "source": "TeD",
                "price_category": "expensive"
            }
        """.trimIndent()))

        val video = SearchHitConverter.convert(searchHit)


        assertThat(video).isEqualTo(Video(
                id = "14",
                title = "The title",
                description = "The description"
        ))
    }
}