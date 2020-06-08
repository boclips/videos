package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.videos.model.SourceType
import com.boclips.search.service.domain.videos.model.SubjectMetadata
import com.boclips.search.service.domain.videos.model.SubjectsMetadata
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
                "rawTitle": "The title",
                "description": "The description",
                "contentProvider": "TED Talks",
                "contentPartnerId": "123",
                "price_category": "expensive",
                "duration": "02:01:20",
                "keywords": ["k1","k2"],
                "tags": ["news", "classroom"],
                "durationSeconds": 10,
                "source": "Boclips",
                "transcript": "A great transcript",
                "ageRangeMin": "3",
                "ageRangeMax": "11",
                "ageRange": [3,4,5,6,7,8,9,10,11],
                "type": "NEWS",
                "types": ["NEWS"],
                "subjectIds": ["boring-subject-id"],
                "subjectNames": ["boring-subject-name"],
                "eligibleForStream": true,
                "eligibleForDownload": true,
                "attachmentTypes": ["ACTIVITY"],
                "deactivated": false
            }
        """.trimIndent()
            )
        )

        val video = VideoDocumentConverter.fromSearchHit(searchHit)

        assertThat(video).isEqualTo(
            VideoDocument(
                id = "14",
                title = "The title",
                rawTitle = "The title",
                description = "The description",
                contentProvider = "TED Talks",
                contentPartnerId = "123",
                releaseDate = null,
                keywords = listOf("k1", "k2"),
                tags = listOf("news", "classroom"),
                durationSeconds = 10,
                source = "Boclips",
                transcript = "A great transcript",
                ageRangeMin = 3,
                ageRangeMax = 11,
                ageRange = (3..11).toList(),
                type = "NEWS",
                subjectIds = setOf("boring-subject-id"),
                subjectNames = setOf("boring-subject-name"),
                promoted = null,
                meanRating = null,
                subjectsSetManually = null,
                eligibleForStream = true,
                eligibleForDownload = true,
                attachmentTypes = setOf("ACTIVITY"),
                deactivated = false,
                types = listOf("NEWS")
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
                "rawTitle": "The title",
                "description": "The description",
                "contentProvider": "TED Talks",
                "contentPartnerId": "123",  
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
            rawTitle = "title",
            description = "description",
            contentProvider = "contentProvider",
            contentPartnerId = "cp-123",
            releaseDate = LocalDate.of(2019, 9, 12),
            keywords = listOf("keyword"),
            tags = listOf("tag"),
            durationSeconds = 120,
            source = SourceType.BOCLIPS,
            transcript = "transcript",
            ageRangeMin = 10,
            ageRangeMax = 16,
            type = VideoType.INSTRUCTIONAL,
            subjects = SubjectsMetadata(
                items = setOf(SubjectMetadata(id = "subjectId", name = "subjectName")),
                setManually = false
            ),
            promoted = null,
            meanRating = 3.8,
            eligibleForStream = false,
            eligibleForDownload = true,
            attachmentTypes = emptySet(),
            deactivated = false,
            types = listOf(VideoType.INSTRUCTIONAL)

        )

        val document = VideoDocumentConverter.fromVideo(video)

        assertThat(document).isEqualTo(
            VideoDocument(
                id = "id",
                title = "title",
                rawTitle = "title",
                description = "description",
                contentProvider = "contentProvider",
                contentPartnerId = "cp-123",
                releaseDate = LocalDate.of(2019, 9, 12),
                keywords = listOf("keyword"),
                tags = listOf("tag"),
                durationSeconds = 120,
                source = "BOCLIPS",
                transcript = "transcript",
                ageRangeMin = 10,
                ageRangeMax = 16,
                ageRange = (10..16).toList(),
                type = "INSTRUCTIONAL",
                types = listOf("INSTRUCTIONAL"),
                subjectIds = setOf("subjectId"),
                subjectNames = setOf("subjectName"),
                promoted = null,
                meanRating = 3.8,
                subjectsSetManually = false,
                eligibleForStream = false,
                eligibleForDownload = true,
                attachmentTypes = emptySet(),
                deactivated = false
            )
        )
    }

    @Test
    fun `converts null age ranges to an empty array`() {
        val video = VideoMetadata(
            id = "id",
            title = "title",
            rawTitle = "title",
            description = "description",
            contentProvider = "contentProvider",
            contentPartnerId = "cp-123",
            releaseDate = LocalDate.of(2019, 9, 12),
            keywords = listOf("keyword"),
            tags = listOf("tag"),
            durationSeconds = 120,
            source = SourceType.BOCLIPS,
            transcript = "transcript",
            ageRangeMin = null,
            ageRangeMax = null,
            type = VideoType.INSTRUCTIONAL,
            subjects = SubjectsMetadata(
                items = setOf(SubjectMetadata(id = "subjectId", name = "subjectName")),
                setManually = false
            ),
            promoted = null,
            meanRating = 3.8,
            eligibleForStream = true,
            eligibleForDownload = true,
            attachmentTypes = emptySet(),
            deactivated = false,
            types = listOf(VideoType.INSTRUCTIONAL)
        )

        val document = VideoDocumentConverter.fromVideo(video)

        assertThat(document.ageRange).isEmpty()
    }

    @Test
    fun `does not default eligibleForStream if present`() {
        val searchHit = SearchHit(14).sourceRef(
            BytesArray(
                """
            {
                "id": "14",
                "title": "The title",
                "rawTitle": "The title",
                "description": "The description",
                "contentProvider": "TED Talks",
                "contentPartnerId": "123",
                "eligibleForStream": false,
                "keywords": ["k1","k2"],
                "tags": ["news", "classroom"],
                "durationSeconds": 10,
                "source": "Boclips"
            }
        """.trimIndent()
            )
        )

        val video = VideoDocumentConverter.fromSearchHit(searchHit)

        assertThat(video.eligibleForStream).isFalse()
    }
}
