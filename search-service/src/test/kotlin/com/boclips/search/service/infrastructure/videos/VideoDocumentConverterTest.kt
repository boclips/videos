package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.videos.model.SourceType
import com.boclips.search.service.domain.videos.model.SubjectMetadata
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.domain.videos.model.VideoType
import org.assertj.core.api.Assertions.assertThat
import org.elasticsearch.common.bytes.BytesArray
import org.elasticsearch.search.SearchHit
import org.junit.jupiter.api.Test
import java.time.LocalDate

class VideoDocumentConverterTest {

    @Test
    fun fromSearchHit() {
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
                "type": "NEWS",
                "subjectIds": ["boring-subject-id"],
                "subjectNames": ["boring-subject-name"]
            }
        """.trimIndent()
            )
        )

        val video = VideoDocumentConverter.fromSearchHit(searchHit)

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
                type = "NEWS",
                subjectIds = setOf("boring-subject-id"),
                subjectNames = setOf("boring-subject-name"),
                promoted = null
            )
        )
    }

    @Test
    fun `fromSearchHit when no transcript`() {
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

        val video = VideoDocumentConverter.fromSearchHit(searchHit)

        assertThat(video.transcript).isNull()
    }

    @Test
    fun fromVideo() {
        val video = VideoMetadata(
            id = "id",
            title = "title",
            description = "description",
            contentProvider = "contentProvider",
            releaseDate = LocalDate.of(2019, 9, 12),
            keywords = listOf("keyword"),
            tags = listOf("tag"),
            durationSeconds = 120,
            source = SourceType.BOCLIPS,
            transcript = "transcript",
            ageRangeMin = 10,
            ageRangeMax = 16,
            type = VideoType.INSTRUCTIONAL,
            subjects = setOf(SubjectMetadata(id = "subjectId", name = "subjectName")),
            promoted = null
        )

        val document = VideoDocumentConverter.fromVideo(video)

        assertThat(document).isEqualTo(VideoDocument(
            id = "id",
            title = "title",
            description = "description",
            contentProvider = "contentProvider",
            releaseDate = LocalDate.of(2019, 9, 12),
            keywords = listOf("keyword"),
            tags = listOf("tag"),
            durationSeconds = 120,
            source = "BOCLIPS",
            transcript = "transcript",
            ageRangeMin = 10,
            ageRangeMax = 16,
            type = "INSTRUCTIONAL",
            subjectIds = setOf("subjectId"),
            subjectNames = setOf("subjectName"),
            promoted = null
        ))

    }
}
