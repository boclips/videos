package com.boclips.search.service.infrastructure.contract

import com.boclips.search.service.domain.common.IndexReader
import com.boclips.search.service.domain.common.IndexWriter
import com.boclips.search.service.domain.common.model.PaginatedIndexSearchRequest
import com.boclips.search.service.domain.videos.model.VideoAccessRuleQuery
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.domain.videos.model.VoiceType
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
import org.assertj.core.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource

class VideoSearchVoiceTypesContractTest : EmbeddedElasticSearchIntegrationTest() {
    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `returns all videos when type is not specified`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.safeRebuildIndex(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "May Dancing", isVoiced = true),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    title = "Trump Dancing",
                    isVoiced = false
                ),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    title = "Johnson Dancing",
                    isVoiced = null
                )
            )
        )

        val result = queryService.search(
            PaginatedIndexSearchRequest(
                query = VideoQuery(
                    videoAccessRuleQuery = VideoAccessRuleQuery(includedVoiceType = emptySet())
                )
            )
        )

        Assertions.assertThat(result.elements).containsOnly("1", "2", "3")
        Assertions.assertThat(result.counts.totalHits).isEqualTo(3)
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `filters out non-voiced content`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.safeRebuildIndex(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "May Dancing", isVoiced = true),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    title = "Trump Dancing",
                    isVoiced = false
                ),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    title = "Johnson Dancing",
                    isVoiced = null
                )
            )
        )

        val result = queryService.search(
            PaginatedIndexSearchRequest(
                query = VideoQuery(
                    videoAccessRuleQuery = VideoAccessRuleQuery(
                        includedVoiceType = setOf(VoiceType.WITH, VoiceType.UNKNOWN)
                    )

                )
            )
        )

        Assertions.assertThat(result.elements).containsExactlyInAnyOrder("1", "3")
        Assertions.assertThat(result.counts.totalHits).isEqualTo(2)
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `filters out unknown voiced content`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.safeRebuildIndex(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "May Dancing", isVoiced = true),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    title = "Trump Dancing",
                    isVoiced = false
                ),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    title = "Johnson Dancing",
                    isVoiced = null
                )
            )
        )

        val result = queryService.search(
            PaginatedIndexSearchRequest(
                query = VideoQuery(
                    videoAccessRuleQuery = VideoAccessRuleQuery(
                        includedVoiceType = setOf(VoiceType.WITH, VoiceType.WITHOUT)
                    )
                )
            )
        )

        Assertions.assertThat(result.elements).containsExactlyInAnyOrder("1", "2")
        Assertions.assertThat(result.counts.totalHits).isEqualTo(2)
    }
}
