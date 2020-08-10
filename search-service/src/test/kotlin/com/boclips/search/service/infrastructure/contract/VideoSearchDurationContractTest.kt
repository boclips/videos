package com.boclips.search.service.infrastructure.contract

import com.boclips.search.service.domain.common.IndexReader
import com.boclips.search.service.domain.common.IndexWriter
import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.domain.videos.model.AccessRuleQuery
import com.boclips.search.service.domain.videos.model.DurationRange
import com.boclips.search.service.domain.videos.model.UserQuery
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
import org.assertj.core.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import java.time.Duration

class VideoSearchDurationContractTest : EmbeddedElasticSearchIntegrationTest() {

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `can filter by duration lower bound`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "0", description = "Zeroth world war", durationSeconds = 1),
                SearchableVideoMetadataFactory.create(id = "1", description = "First world war", durationSeconds = 5),
                SearchableVideoMetadataFactory.create(id = "2", description = "Second world war", durationSeconds = 10),
                SearchableVideoMetadataFactory.create(id = "3", description = "Third world war", durationSeconds = 15)
            )
        )

        val results = queryService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    "World war",
                    userQuery = UserQuery(durationRanges = listOf(DurationRange(min = Duration.ofSeconds(10)))),
                    accessRuleQuery = AccessRuleQuery()
                )
            )
        )

        Assertions.assertThat(results.elements).containsAll(listOf("2", "3"))
        Assertions.assertThat(results.elements).doesNotContainAnyElementsOf(listOf("0", "1"))
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `can filter by duration upper bound`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "0", description = "Zeroth world war", durationSeconds = 1),
                SearchableVideoMetadataFactory.create(id = "1", description = "First world war", durationSeconds = 5),
                SearchableVideoMetadataFactory.create(id = "2", description = "Second world war", durationSeconds = 10),
                SearchableVideoMetadataFactory.create(id = "3", description = "Third world war", durationSeconds = 15)
            )
        )

        val results =
            queryService.search(
                PaginatedSearchRequest(
                    query = VideoQuery(
                        "World war",
                        userQuery = UserQuery(
                            durationRanges = listOf(
                                DurationRange(
                                    min = Duration.ofSeconds(0),
                                    max = Duration.ofSeconds(9)
                                )
                            )
                        ), accessRuleQuery = AccessRuleQuery()
                    )
                )
            )

        Assertions.assertThat(results.elements).containsAll(listOf("0", "1"))
        Assertions.assertThat(results.elements).doesNotContainAnyElementsOf(listOf("2", "3"))
    }
}
