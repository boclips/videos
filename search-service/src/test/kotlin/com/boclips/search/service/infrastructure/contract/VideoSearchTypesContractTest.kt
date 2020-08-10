package com.boclips.search.service.infrastructure.contract

import com.boclips.search.service.domain.common.IndexReader
import com.boclips.search.service.domain.common.IndexWriter
import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.domain.videos.model.AccessRuleQuery
import com.boclips.search.service.domain.videos.model.UserQuery
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.domain.videos.model.VideoType
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
import org.assertj.core.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource

class VideoSearchTypesContractTest : EmbeddedElasticSearchIntegrationTest() {
    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `returns all videos when type is not specified`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.safeRebuildIndex(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "May Dancing", types = listOf(VideoType.NEWS)),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    title = "Trump Dancing",
                    types = listOf(VideoType.STOCK)
                ),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    title = "Johnson Dancing",
                    types = listOf(VideoType.INSTRUCTIONAL)
                )
            )
        )

        val result = queryService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    accessRuleQuery = AccessRuleQuery(includedTypes = emptySet())
                )
            )
        )

        Assertions.assertThat(result.elements).containsOnly("1", "2", "3")
        Assertions.assertThat(result.counts.totalHits).isEqualTo(3)
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `filters out videos of excluded video types`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.safeRebuildIndex(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "May Dancing", types = listOf(VideoType.NEWS)),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    title = "Trump Dancing",
                    types = listOf(VideoType.STOCK)
                ),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    title = "Johnson Dancing",
                    types = listOf(VideoType.INSTRUCTIONAL)
                )
            )
        )

        val result = queryService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    accessRuleQuery = AccessRuleQuery(excludedTypes = setOf(VideoType.NEWS, VideoType.STOCK))
                )
            )
        )

        Assertions.assertThat(result.elements).containsOnly("3")
        Assertions.assertThat(result.counts.totalHits).isEqualTo(1)
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `filters out specified video types when retrieving videos by ids`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.safeRebuildIndex(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", types = listOf(VideoType.NEWS)),
                SearchableVideoMetadataFactory.create(id = "2", types = listOf(VideoType.STOCK)),
                SearchableVideoMetadataFactory.create(id = "3", types = listOf(VideoType.INSTRUCTIONAL))
            )
        )

        val result = queryService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    accessRuleQuery = AccessRuleQuery(excludedTypes = setOf(VideoType.NEWS, VideoType.STOCK)),
                    userQuery = UserQuery(
                        ids = setOf("1", "2", "3")
                    )

                )
            )
        )

        Assertions.assertThat(result.elements).containsOnly("3")
        Assertions.assertThat(result.counts.totalHits).isEqualTo(1)
    }
}
