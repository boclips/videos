package com.boclips.search.service.infrastructure.contract

import com.boclips.search.service.domain.common.IndexReader
import com.boclips.search.service.domain.common.IndexWriter
import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.domain.videos.model.VideoAccessRuleQuery
import com.boclips.search.service.domain.videos.model.UserQuery
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.domain.videos.model.VideoType
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
import org.assertj.core.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource

class VideoSearchBestForContractTest : EmbeddedElasticSearchIntegrationTest() {
    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `finds videos by best for tags`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.safeRebuildIndex(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "May Dancing", tags = listOf("news")),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    title = "Beer Trump",
                    description = "Behave like a gentleman, cane like a sponge"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "4",
                    title = "Trump to attack UK",
                    contentProvider = "BBC",
                    tags = listOf("news")
                )
            )
        )

        val result = queryService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    phrase = "Trump",
                    userQuery = UserQuery(
                        bestFor = listOf("news")
                    ),
                    videoAccessRuleQuery = VideoAccessRuleQuery()
                )
            )
        )

        Assertions.assertThat(result.elements).containsExactlyInAnyOrder("4")
        Assertions.assertThat(result.counts.totalHits).isEqualTo(1)
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `returns all results if best for tags not provided`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.safeRebuildIndex(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "May Dancing", tags = listOf("news")),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    title = "Beer Trump",
                    description = "Behave like a gentleman, cane like a sponge"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "4",
                    title = "Trump to attack UK",
                    contentProvider = "BBC",
                    tags = listOf("news")
                )
            )
        )

        val result = queryService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    phrase = "Trump",
                    videoAccessRuleQuery = VideoAccessRuleQuery()
                )
            )
        )

        Assertions.assertThat(result.elements).containsExactlyInAnyOrder("2", "4")
        Assertions.assertThat(result.counts.totalHits).isEqualTo(2)
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `returns all results if empty best for tags are given`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.safeRebuildIndex(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "May Dancing", tags = listOf("news")),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    title = "Beer Trump",
                    description = "Behave like a gentleman, cane like a sponge"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "4",
                    title = "Trump to attack UK",
                    contentProvider = "BBC",
                    tags = listOf("news")
                )
            )
        )

        val result = queryService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    phrase = "Trump",
                    userQuery = UserQuery(
                        bestFor = emptyList()
                    ),
                    videoAccessRuleQuery = VideoAccessRuleQuery()

                )
            )
        )

        Assertions.assertThat(result.elements).containsExactlyInAnyOrder("2", "4")
        Assertions.assertThat(result.counts.totalHits).isEqualTo(2)
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `finds by video type`(
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
                    userQuery = UserQuery(types = setOf(VideoType.NEWS, VideoType.STOCK)),
                    videoAccessRuleQuery = VideoAccessRuleQuery()
                )
            )
        )

        Assertions.assertThat(result.elements).containsOnly("1", "2")
        Assertions.assertThat(result.counts.totalHits).isEqualTo(2)
    }
}
