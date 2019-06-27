package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.model.PaginatedSearchRequest
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration

class ESVideoReadSearchServiceCombinationSearchesIntegrationTest : EmbeddedElasticSearchIntegrationTest() {

    lateinit var readSearchService: ESVideoReadSearchService
    lateinit var writeSearchService: ESVideoWriteSearchService

    @BeforeEach
    internal fun setUp() {
        readSearchService = ESVideoReadSearchService(EmbeddedElasticSearchIntegrationTest.CONFIG.buildClient())
        writeSearchService = ESVideoWriteSearchService(EmbeddedElasticSearchIntegrationTest.CONFIG.buildClient())
    }

    @Test
    fun `no filters return everything`() {
        writeSearchService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "Apple banana candy")
            )
        )
        writeSearchService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "2", title = "candy banana apple")
            )
        )

        val results = readSearchService.search(PaginatedSearchRequest(query = VideoQuery()))

        assertThat(results).hasSize(2)
    }

    @Test
    fun `age, subject`() {
        writeSearchService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    title = "TED",
                    ageRangeMin = 3,
                    ageRangeMax = 15,
                    subjects = setOf("subject-1")
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    title = "TED",
                    ageRangeMin = 7,
                    subjects = setOf("subject-1")
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    title = "TED",
                    ageRangeMin = 3,
                    ageRangeMax = 5,
                    subjects = setOf("subject-2")
                )
            )
        )

        val results = readSearchService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    ageRangeMin = 3,
                    ageRangeMax = 5,
                    subjects = setOf("subject-1")
                )
            )
        )

        assertThat(results).hasSize(1)
        assertThat(results).contains("1")
    }

    @Test
    fun `age, query`() {
        writeSearchService.upsert(
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

        val results = readSearchService.search(
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
    fun `age, include tag`() {
        writeSearchService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    ageRangeMin = 3,
                    ageRangeMax = 5,
                    tags = listOf("classroom")
                ),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    tags = listOf("news"),
                    ageRangeMin = 3,
                    ageRangeMax = 5
                )
            )
        )

        val results = readSearchService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    ageRangeMin = 3,
                    ageRangeMax = 5,
                    includeTags = listOf("news")
                )
            )
        )

        assertThat(results).hasSize(1)
        assertThat(results).containsExactly("3")
    }

    @Test
    fun `age, subject, query`() {
        writeSearchService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    title = "TED",
                    ageRangeMin = 3,
                    ageRangeMax = 15,
                    subjects = setOf("subject-1")
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    title = "TED",
                    ageRangeMin = 7,
                    subjects = setOf("subject-1")
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    title = "Intercom Learning",
                    ageRangeMin = 3,
                    ageRangeMax = 5,
                    subjects = setOf("subject-2")
                )
            )
        )

        val results = readSearchService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    phrase = "Intercom Learning",
                    ageRangeMin = 3,
                    ageRangeMax = 5,
                    subjects = setOf("subject-1")
                )
            )
        )

        assertThat(results).isEmpty()
    }

    @Test
    fun `subject, query`() {
        writeSearchService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    title = "TED",
                    subjects = setOf("subject-one", "subject-two")
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    title = "HELLO",
                    subjects = setOf("subject-two", "subject-three")
                )
            )
        )

        val results = readSearchService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    phrase = "TED",
                    subjects = setOf("subject-two")
                )
            )
        )

        assertThat(results).hasSize(1)
        assertThat(results).contains("1")
    }

    @Test
    fun `tags, subject`() {
        writeSearchService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    title = "TED",
                    subjects = setOf("subject-one", "subject-two"),
                    tags = listOf("classroom")
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    title = "HELLO",
                    subjects = setOf("subject-two", "subject-three"),
                    tags = listOf("news")
                )
            )
        )

        val results = readSearchService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    subjects = setOf("subject-two"),
                    includeTags = listOf("news")
                )
            )
        )

        assertThat(results).hasSize(1)
        assertThat(results).contains("2")
    }

    @Test
    fun `include tag, query`() {
        writeSearchService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "3", description = "candy banana apple"),
                SearchableVideoMetadataFactory.create(
                    id = "4",
                    description = "candy banana apple",
                    tags = listOf("news")
                )
            )
        )

        val results =
            readSearchService.search(
                PaginatedSearchRequest(
                    query = VideoQuery(
                        phrase = "banana",
                        includeTags = listOf("news")
                    )
                )
            )

        assertThat(results).containsExactly("4")
    }

    @Test
    fun `exclude tags, query`() {
        writeSearchService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "3", description = "some random banana isNews"),
                SearchableVideoMetadataFactory.create(
                    id = "4",
                    description = "candy banana apple",
                    tags = listOf("news")
                )
            )
        )

        val results =
            readSearchService.search(
                PaginatedSearchRequest(
                    query = VideoQuery(
                        phrase = "banana",
                        excludeTags = listOf("news")
                    )
                )
            )

        assertThat(results).containsExactly("3")
    }

    @Test
    fun `duration, query`() {
        writeSearchService.upsert(
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

        val results = readSearchService.search(
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
        writeSearchService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    durationSeconds = 10,
                    subjects = setOf("subject-two", "subject-three")
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    durationSeconds = 50,
                    subjects = setOf("subject-two", "subject-three")
                ),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    durationSeconds = 100,
                    subjects = setOf("subject-two", "subject-three")
                )
            )
        )

        val results = readSearchService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    subjects = setOf("subject-two"),
                    minDuration = Duration.ofSeconds(49),
                    maxDuration = Duration.ofSeconds(51)
                )
            )
        )
        assertThat(results).containsExactly("2")
    }
}