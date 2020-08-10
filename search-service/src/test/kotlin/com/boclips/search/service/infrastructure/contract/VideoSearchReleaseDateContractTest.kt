package com.boclips.search.service.infrastructure.contract

import com.boclips.search.service.domain.common.IndexReader
import com.boclips.search.service.domain.common.IndexWriter
import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.domain.videos.model.AccessRuleQuery
import com.boclips.search.service.domain.videos.model.UserQuery
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
import org.assertj.core.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import java.time.LocalDate

class VideoSearchReleaseDateContractTest : EmbeddedElasticSearchIntegrationTest() {

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `can filter by release date range`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "0",
                    description = "Zeroth world war",
                    releaseDate = LocalDate.of(2000, 1, 10)
                ),
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    description = "First world war",
                    releaseDate = LocalDate.of(2002, 1, 1)
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    description = "Second world war",
                    releaseDate = LocalDate.of(2003, 1, 1)
                ),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    description = "Third world war",
                    releaseDate = LocalDate.of(2004, 1, 1)
                )
            )
        )

        val results =
            queryService.search(
                PaginatedSearchRequest(
                    query = VideoQuery(
                        phrase = "World war",
                        userQuery = UserQuery(
                            releaseDateFrom = LocalDate.of(1999, 1, 10),
                            releaseDateTo = LocalDate.of(2002, 1, 10)
                        ),
                        accessRuleQuery = AccessRuleQuery()
                    )
                )
            )

        Assertions.assertThat(results.elements).containsAll(listOf("0", "1"))
        Assertions.assertThat(results.elements).doesNotContainAnyElementsOf(listOf("2", "3"))
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `can filter by release date lower bound`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "0",
                    description = "Zeroth world war",
                    releaseDate = LocalDate.of(2000, 1, 10)
                ),
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    description = "First world war",
                    releaseDate = LocalDate.of(2002, 1, 1)
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    description = "Second world war",
                    releaseDate = LocalDate.of(2003, 1, 1)
                ),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    description = "Third world war",
                    releaseDate = LocalDate.of(2004, 1, 1)
                )
            )
        )

        val results =
            queryService.search(
                PaginatedSearchRequest(
                    query = VideoQuery(
                        phrase = "World war",
                        userQuery = UserQuery(
                            releaseDateFrom = LocalDate.of(2002, 5, 5)
                        ),
                        accessRuleQuery = AccessRuleQuery()
                    )
                )
            )

        Assertions.assertThat(results.elements).containsAll(listOf("2", "3"))
        Assertions.assertThat(results.elements).doesNotContainAnyElementsOf(listOf("1", "0"))
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `can filter by release date upper bound`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "0",
                    description = "Zeroth world war",
                    releaseDate = LocalDate.of(2000, 1, 10)
                ),
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    description = "First world war",
                    releaseDate = LocalDate.of(2002, 1, 1)
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    description = "Second world war",
                    releaseDate = LocalDate.of(2003, 1, 1)
                ),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    description = "Third world war",
                    releaseDate = LocalDate.of(2004, 1, 1)
                )
            )
        )

        val results =
            queryService.search(
                PaginatedSearchRequest(
                    query = VideoQuery(
                        phrase = "World war",
                        userQuery = UserQuery(
                            releaseDateTo = LocalDate.of(2002, 5, 5)
                        ),
                        accessRuleQuery = AccessRuleQuery()
                    )
                )
            )

        Assertions.assertThat(results.elements).containsAll(listOf("1", "0"))
        Assertions.assertThat(results.elements).doesNotContainAnyElementsOf(listOf("2", "3"))
    }
}
