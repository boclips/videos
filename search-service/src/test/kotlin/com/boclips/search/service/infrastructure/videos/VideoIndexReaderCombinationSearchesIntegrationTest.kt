package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
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
        videoIndexWriter = VideoIndexWriter.createTestInstance(esClient)
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

        assertThat(results).hasSize(2)
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
                    id = "2",
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

        assertThat(results).hasSize(1)
        assertThat(results).contains("1")
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

        assertThat(results).hasSize(1)
        assertThat(results).containsExactly("2")
    }

    @Test
    fun `query, type`() {
        videoIndexWriter.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    title = "TED TALK 1 ",
                    type = VideoType.STOCK
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    title = "OTHER VIDEO 1",
                    type = VideoType.STOCK
                ),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    title = "TED TALK 2",
                    type = VideoType.INSTRUCTIONAL
                ),
                SearchableVideoMetadataFactory.create(
                    id = "4",
                    title = "TED TALK 3",
                    type = VideoType.NEWS
                ),
                SearchableVideoMetadataFactory.create(
                    id = "5",
                    title = "OTHER VIDEO 2",
                    type = VideoType.NEWS
                )
            )
        )

        val result = videoIndexReader.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    phrase = "TED",
                    type = setOf(VideoType.STOCK, VideoType.NEWS)
                )
            )
        )

        assertThat(result).containsOnly("1", "4")
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
                    id = "2",
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

        assertThat(results).isEmpty()
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

        assertThat(results).hasSize(1)
        assertThat(results).contains("1")
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

        assertThat(results).hasSize(1)
        assertThat(results).contains("2")
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

        assertThat(results).containsExactly("4")
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
                    minDuration = Duration.ofSeconds(40),
                    maxDuration = Duration.ofSeconds(60)
                )
            )
        )
        assertThat(results).containsExactly("2")
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
                    minDuration = Duration.ofSeconds(49),
                    maxDuration = Duration.ofSeconds(51)
                )
            )
        )
        assertThat(results).containsExactly("2")
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
        assertThat(results).containsExactly("1")
    }
}
