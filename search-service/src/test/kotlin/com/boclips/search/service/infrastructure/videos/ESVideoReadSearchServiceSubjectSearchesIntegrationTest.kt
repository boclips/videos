package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.model.PaginatedSearchRequest
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.jupiter.api.BeforeEach

class ESVideoReadSearchServiceSubjectSearchesIntegrationTest : EmbeddedElasticSearchIntegrationTest() {

    lateinit var readSearchService: ESVideoReadSearchService
    lateinit var writeSearchService: ESVideoWriteSearchService

    @BeforeEach
    internal fun setUp() {
        readSearchService = ESVideoReadSearchService(EmbeddedElasticSearchIntegrationTest.CONFIG.buildClient())
        writeSearchService = ESVideoWriteSearchService(EmbeddedElasticSearchIntegrationTest.CONFIG.buildClient())
    }

    @Test
    fun `videos that match a given subject`() {
        writeSearchService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    title = "TED",
                    subjects = setOf("my-fancy-subject")
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    title = "TED",
                    subjects = setOf("my-less-fancy-subject")
                )
            )
        )

        val results = readSearchService.search(
            PaginatedSearchRequest(
                query = VideoQuery(subjects = setOf("my-fancy-subject"))
            )
        )

        assertThat(results).hasSize(1)
        assertThat(results).contains("1")
    }

    @Test
    fun `videos with any matching subject`() {
        writeSearchService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    title = "TED",
                    subjects = setOf("subject-one")
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    title = "TED",
                    subjects = setOf("subject-two")
                ),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    title = "TED",
                    subjects = setOf("subject-three")
                )
            )
        )

        val results = readSearchService.search(
            PaginatedSearchRequest(
                query = VideoQuery(subjects = setOf("subject-one", "subject-two"))
            )
        )

        assertThat(results).hasSize(2)
        assertThat(results).contains("1")
        assertThat(results).contains("2")
    }

    @Test
    fun `videos tagged with multiple subjects`() {
        writeSearchService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    title = "TED",
                    subjects = setOf("subject-one", "subject-two")
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    title = "TED",
                    subjects = setOf("subject-two", "subject-three")
                ),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    title = "TED",
                    subjects = setOf("subject-three")
                )
            )
        )

        val results = readSearchService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    subjects = setOf("subject-two")
                )
            )
        )

        assertThat(results).hasSize(2)
        assertThat(results).contains("1")
        assertThat(results).contains("2")
    }

    @Test
    fun `does not match subjects`() {
        writeSearchService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    title = "TED",
                    subjects = setOf("maths-123")
                )
            )
        )

        val results = readSearchService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    phrase = "",
                    subjects = setOf("biology-987")
                )
            )
        )

        assertThat(results).hasSize(0)
    }
}