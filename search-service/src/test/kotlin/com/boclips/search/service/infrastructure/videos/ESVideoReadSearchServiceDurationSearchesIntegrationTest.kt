package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.domain.model.PaginatedSearchRequest
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.jupiter.api.BeforeEach
import java.time.Duration

class ESVideoReadSearchServiceDurationSearchesIntegrationTest : EmbeddedElasticSearchIntegrationTest() {

    lateinit var readSearchService: ESVideoReadSearchService
    lateinit var writeSearchService: ESVideoWriteSearchService

    @BeforeEach
    internal fun setUp() {
        readSearchService = ESVideoReadSearchService(EmbeddedElasticSearchIntegrationTest.CONFIG.buildClient())
        writeSearchService = ESVideoWriteSearchService(EmbeddedElasticSearchIntegrationTest.CONFIG.buildClient())
    }

    @Test
    fun `duration range matches`() {
        writeSearchService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", durationSeconds = 120),
                SearchableVideoMetadataFactory.create(id = "2", durationSeconds = 100),
                SearchableVideoMetadataFactory.create(id = "3", durationSeconds = 60)
            )
        )

        val results =
            readSearchService.search(
                PaginatedSearchRequest(
                    query = VideoQuery(
                        minDuration = Duration.ofSeconds(60),
                        maxDuration = Duration.ofSeconds(110)
                    )
                )
            )

        assertThat(results.size).isEqualTo(2)
        assertThat(results).containsExactlyInAnyOrder("2", "3")
    }

    @Test
    fun `duration range no upper bound`() {
        writeSearchService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", durationSeconds = 120),
                SearchableVideoMetadataFactory.create(id = "2", durationSeconds = 100),
                SearchableVideoMetadataFactory.create(id = "3", durationSeconds = 40)
            )
        )

        val results =
            readSearchService.search(
                PaginatedSearchRequest(
                    query = VideoQuery(
                        minDuration = Duration.ofSeconds(60)
                    )
                )
            )

        assertThat(results.size).isEqualTo(2)
        assertThat(results).containsExactlyInAnyOrder("1", "2")
    }

    @Test
    fun `duration range no lower bound`() {
        writeSearchService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", durationSeconds = 120),
                SearchableVideoMetadataFactory.create(id = "2", durationSeconds = 60),
                SearchableVideoMetadataFactory.create(id = "3", durationSeconds = 100)
            )
        )

        val results =
            readSearchService.search(
                PaginatedSearchRequest(
                    query = VideoQuery(maxDuration = Duration.ofSeconds(110))
                )
            )

        assertThat(results.size).isEqualTo(2)
        assertThat(results).containsExactlyInAnyOrder("2", "3")
    }
}