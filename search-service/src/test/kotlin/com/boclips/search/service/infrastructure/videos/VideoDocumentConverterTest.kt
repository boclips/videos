package com.boclips.search.service.infrastructure.videos

import org.assertj.core.api.Assertions.assertThat
import org.elasticsearch.common.bytes.BytesArray
import org.elasticsearch.search.SearchHit
import org.junit.jupiter.api.Test

class VideoDocumentConverterTest {
    private val elasticSearchResultConverter = VideoDocumentConverter()

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
                "source": "Boclips",
                "transcript": "A great transcript",
                "ageRangeMin": "3",
                "ageRangeMax": "11",
                "subjectIds": ["boring-subject"]
            }
        """.trimIndent()
            )
        )

        val video = elasticSearchResultConverter.convert(searchHit)

        assertThat(video).isEqualTo(
            VideoDocument(
                id = "14",
                title = "The title",
                description = "The description",
                contentProvider = "TED Talks",
                releaseDate = null,
                keywords = listOf("k1", "k2"),
                tags = listOf("news", "classroom"),
                durationSeconds = 10,
                source = "Boclips",
                transcript = "A great transcript",
                ageRangeMin = 3,
                ageRangeMax = 11,
                subjectIds = setOf("boring-subject")
            )
        )
    }

    @Test
    fun `convert search hit with no transcript`() {
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

        assertThat(video.transcript).isNull()
    }
}
