package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.domain.videos.model.SourceType
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDate
import java.time.Month

class VideoIndexReaderContentPartnerSearchesIntegrationTest : EmbeddedElasticSearchIntegrationTest() {
    private lateinit var videoIndexReader: VideoIndexReader
    private lateinit var videoIndexWriter: VideoIndexWriter

    @BeforeEach
    fun setUp() {
        videoIndexReader = VideoIndexReader(esClient)
        videoIndexWriter = VideoIndexWriter(esClient)
    }

    @Test
    fun `content partner matches exactly and has no excluded tags`() {
        val contentProvider = "Bozeman Science"

        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    contentProvider = contentProvider,
                    tags = listOf("news")
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    contentProvider = contentProvider,
                    tags = emptyList()
                )
            )
        )

        val results =
            videoIndexReader.search(
                PaginatedSearchRequest(
                    query = VideoQuery(
                        phrase = contentProvider,
                        excludeTags = listOf("news")
                    )
                )
            )

        assertThat(results).containsExactly("2")
    }

    @Test
    fun `content partner matches exactly and has included tags`() {
        val contentProvider = "Bozeman Science"

        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    contentProvider = contentProvider,
                    tags = listOf("education")
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    contentProvider = contentProvider,
                    tags = emptyList()
                )
            )
        )

        val results = videoIndexReader.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    phrase = contentProvider,
                    includeTags = listOf("education")
                )
            )
        )

        assertThat(results).containsExactly("1")
    }

    @Test
    fun `rank content partner matches above other field matches`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "TED-Ed"),
                SearchableVideoMetadataFactory.create(id = "2", description = "TED-Ed"),
                SearchableVideoMetadataFactory.create(id = "3", contentProvider = "TED-Ed"),
                SearchableVideoMetadataFactory.create(id = "4", keywords = listOf("TED-Ed")),
                SearchableVideoMetadataFactory.create(
                    id = "5",
                    title = "TED-Ed",
                    description = "TED-Ed",
                    keywords = listOf("TED-Ed")
                )
            )
        )

        val results = videoIndexReader.search(
            PaginatedSearchRequest(query = VideoQuery(phrase = "Ted-ed"))
        )

        assertThat(results).startsWith("3")
    }

    @Test
    fun `can filter by duration bound`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "0",
                    description = "Zeroth world war",
                    durationSeconds = 1,
                    contentProvider = "TED"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    description = "First world war",
                    durationSeconds = 5,
                    contentProvider = "TED"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    description = "Second world war",
                    durationSeconds = 10,
                    contentProvider = "TED"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    description = "Third world war",
                    durationSeconds = 15,
                    contentProvider = "TED"
                )
            )
        )

        val results = videoIndexReader.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    "TED",
                    minDuration = Duration.ofSeconds(5),
                    maxDuration = Duration.ofSeconds(10)
                )
            )
        )

        assertThat(results).containsExactlyInAnyOrder("1", "2")
    }

    @Test
    fun `can filter by subject`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "0",
                    subjects = setOf("Maths"),
                    contentProvider = "TED"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    subjects = setOf("History"),
                    contentProvider = "TED"
                )
            )
        )

        val results = videoIndexReader.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    "TED",
                    subjects = setOf("History")
                )
            )
        )

        assertThat(results).containsExactlyInAnyOrder("1")
    }

    @Test
    fun `can filter by age range`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "0",
                    ageRangeMax = 5,
                    ageRangeMin = 2,
                    contentProvider = "TED"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    ageRangeMax = 9,
                    ageRangeMin = 14,
                    contentProvider = "TED"
                )
            )
        )

        val results = videoIndexReader.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    "TED",
                    ageRangeMin = 2,
                    ageRangeMax = 5
                )
            )
        )

        assertThat(results).containsExactlyInAnyOrder("0")
    }

    @Test
    fun `can filter by release date`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "0",
                    releaseDate = LocalDate.of(2019, Month.JANUARY, 10),
                    contentProvider = "TED"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    releaseDate = LocalDate.of(2019, Month.MAY, 10),
                    contentProvider = "TED"
                )
            )
        )

        val results = videoIndexReader.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    "TED",
                    releaseDateFrom = LocalDate.of(2019, Month.JANUARY, 1),
                    releaseDateTo = LocalDate.of(2019, Month.FEBRUARY, 1)
                )
            )
        )

        assertThat(results).containsExactlyInAnyOrder("0")
    }

    @Test
    fun `can filter by source`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "0",
                    source = SourceType.BOCLIPS,
                    contentProvider = "TED"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    source = SourceType.YOUTUBE,
                    contentProvider = "TED"
                )
            )
        )

        val results = videoIndexReader.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    phrase = "TED",
                    source = SourceType.BOCLIPS
                )
            )
        )

        assertThat(results).containsExactlyInAnyOrder("0")
    }
}