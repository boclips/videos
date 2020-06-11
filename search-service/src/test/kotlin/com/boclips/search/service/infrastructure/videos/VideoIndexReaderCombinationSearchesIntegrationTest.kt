package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.domain.videos.model.AgeRange
import com.boclips.search.service.domain.videos.model.DurationRange
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.domain.videos.model.VideoType
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest

import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
import com.boclips.search.service.testsupport.TestFactories.createSubjectMetadata
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration

class VideoIndexReaderCombinationSearchesIntegrationTest : EmbeddedElasticSearchIntegrationTest() {
    private lateinit var videoIndexReader: VideoIndexReader
    private lateinit var videoIndexWriter: VideoIndexWriter

    @BeforeEach
    fun setUp() {
        videoIndexReader = VideoIndexReader(esClient)
        videoIndexWriter = VideoIndexWriter.createTestInstance(esClient, 20)
    }

    @Test
    fun `no filters return everything`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "Apple banana candy")
            )
        )
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "2", title = "candy banana apple")
            )
        )

        val results = videoIndexReader.search(PaginatedSearchRequest(query = VideoQuery()))

        assertThat(results.elements).hasSize(2)
    }

    @Test
    fun `age, subject`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    title = "TED",
                    ageRangeMin = 3,
                    ageRangeMax = 5,
                    subjects = setOf(createSubjectMetadata("subject-1"))
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    title = "TED",
                    ageRangeMin = 7,
                    subjects = setOf(createSubjectMetadata("subject-1"))
                ),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    title = "TED",
                    ageRangeMin = 3,
                    ageRangeMax = 5,
                    subjects = setOf(createSubjectMetadata("subject-2"))
                )
            )
        )

        val results = videoIndexReader.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    ageRangeMin = 3,
                    ageRangeMax = 5,
                    subjectIds = setOf("subject-1")
                )
            )
        )

        assertThat(results.elements).hasSize(1)
        assertThat(results.elements).contains("1")
    }

    @Test
    fun `age, query`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    title = "TED",
                    ageRangeMin = 3,
                    ageRangeMax = 5
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    title = "Intercom Learning",
                    ageRangeMin = 3,
                    ageRangeMax = 5
                )
            )
        )

        val results = videoIndexReader.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    phrase = "Intercom Learning",
                    ageRangeMin = 3,
                    ageRangeMax = 5
                )
            )
        )

        assertThat(results.elements).hasSize(1)
        assertThat(results.elements).containsExactly("2")
    }

    @Test
    fun `query, type`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    title = "TED TALK 1 ",
                    types = listOf(VideoType.STOCK)
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    title = "OTHER VIDEO 1",
                    types = listOf(VideoType.STOCK)
                ),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    title = "TED TALK 2",
                    types = listOf(VideoType.INSTRUCTIONAL)
                ),
                SearchableVideoMetadataFactory.create(
                    id = "4",
                    title = "TED TALK 3",
                    types = listOf(VideoType.NEWS)
                ),
                SearchableVideoMetadataFactory.create(
                    id = "5",
                    title = "OTHER VIDEO 2",
                    types = listOf(VideoType.NEWS)
                )
            )
        )

        val results = videoIndexReader.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    phrase = "TED",
                    includedTypes = setOf(VideoType.STOCK, VideoType.NEWS)
                )
            )
        )

        assertThat(results.elements).containsOnly("1", "4")
    }

    @Test
    fun `age, subject, query`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    title = "TED",
                    ageRangeMin = 3,
                    ageRangeMax = 15,
                    subjects = setOf(createSubjectMetadata("subject-1"))
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    title = "TED",
                    ageRangeMin = 7,
                    subjects = setOf(createSubjectMetadata("subject-1"))
                ),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    title = "Intercom Learning",
                    ageRangeMin = 3,
                    ageRangeMax = 5,
                    subjects = setOf(createSubjectMetadata("subject-2"))
                )
            )
        )

        val results = videoIndexReader.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    phrase = "Intercom Learning",
                    ageRangeMin = 3,
                    ageRangeMax = 5,
                    subjectIds = setOf("subject-1")
                )
            )
        )

        assertThat(results.elements).isEmpty()
    }

    @Test
    fun `age range, subject, query`() {
        val subject = createSubjectMetadata("subject-1")

        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    title = "elephant",
                    ageRangeMin = 3,
                    ageRangeMax = 5,
                    subjects = setOf(subject)
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    title = "elephant",
                    ageRangeMin = 7,
                    ageRangeMax = 12,
                    subjects = setOf(subject)
                ),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    title = "elephant",
                    ageRangeMin = 3,
                    ageRangeMax = 5
                )
            )
        )

        val results = videoIndexReader.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    phrase = "elephant",
                    ageRanges = listOf(AgeRange(3, 6), AgeRange(7, 8)),
                    subjectIds = setOf("subject-1")
                )
            )
        )

        assertThat(results.elements).containsExactly("1", "2")
    }

    @Test
    fun `subject, query`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    title = "TED",
                    subjects = setOf(createSubjectMetadata("subject-one"), createSubjectMetadata("subject-two"))
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    title = "HELLO",
                    subjects = setOf(createSubjectMetadata("subject-two"), createSubjectMetadata("subject-three"))
                )
            )
        )

        val results = videoIndexReader.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    phrase = "TED",
                    subjectIds = setOf("subject-two")
                )
            )
        )

        assertThat(results.elements).hasSize(1)
        assertThat(results.elements).contains("1")
    }

    @Test
    fun `best for tag, subject`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    title = "TED",
                    subjects = setOf(createSubjectMetadata("subject-one"), createSubjectMetadata("subject-two")),
                    tags = listOf("explainer")
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    title = "HELLO",
                    subjects = setOf(createSubjectMetadata("subject-two"), createSubjectMetadata("subject-three")),
                    tags = listOf("other")
                )
            )
        )

        val results = videoIndexReader.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    subjectIds = setOf("subject-two"),
                    bestFor = listOf("other")
                )
            )
        )

        assertThat(results.elements).hasSize(1)
        assertThat(results.elements).contains("2")
    }

    @Test
    fun `best for tag, query`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "3", description = "candy banana apple"),
                SearchableVideoMetadataFactory.create(
                    id = "4",
                    description = "candy banana apple",
                    tags = listOf("explainer")
                )
            )
        )

        val results =
            videoIndexReader.search(
                PaginatedSearchRequest(
                    query = VideoQuery(
                        phrase = "banana",
                        bestFor = listOf("explainer")
                    )
                )
            )

        assertThat(results.elements).containsExactly("4")
    }

    @Test
    fun `duration, query`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "0", durationSeconds = 50),
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    durationSeconds = 10,
                    title = "matching-query"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    durationSeconds = 50,
                    title = "matching-query"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    durationSeconds = 100,
                    title = "matching-query"
                )
            )
        )

        val results = videoIndexReader.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    phrase = "matching-query",
                    durationRanges = listOf(DurationRange(min = Duration.ofSeconds(40), max = Duration.ofSeconds(60)))
                )
            )
        )
        assertThat(results.elements).containsExactly("2")
    }

    @Test
    fun `duration, subject`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    durationSeconds = 10,
                    subjects = setOf(createSubjectMetadata("subject-two"), createSubjectMetadata("subject-three"))
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    durationSeconds = 50,
                    subjects = setOf(createSubjectMetadata("subject-two"), createSubjectMetadata("subject-three"))
                ),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    durationSeconds = 100,
                    subjects = setOf(createSubjectMetadata("subject-two"), createSubjectMetadata("subject-three"))
                )
            )
        )

        val results = videoIndexReader.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    subjectIds = setOf("subject-two"),
                    durationRanges = listOf(DurationRange(min = Duration.ofSeconds(49), max = Duration.ofSeconds(51)))
                )
            )
        )
        assertThat(results.elements).containsExactly("2")
    }

    @Test
    fun `promoted, subject`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    subjects = setOf(createSubjectMetadata("subject-two")),
                    promoted = true
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    subjects = setOf(createSubjectMetadata("subject-one")),
                    promoted = true
                ),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    subjects = setOf(createSubjectMetadata("subject-two"), createSubjectMetadata("subject-one"))
                )
            )
        )

        val results = videoIndexReader.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    subjectIds = setOf("subject-two"),
                    promoted = true
                )
            )
        )
        assertThat(results.elements).containsExactly("1")
    }
}
