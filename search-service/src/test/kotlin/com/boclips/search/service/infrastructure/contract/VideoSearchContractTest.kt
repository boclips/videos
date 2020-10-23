package com.boclips.search.service.infrastructure.contract

import com.boclips.search.service.domain.common.IndexReader
import com.boclips.search.service.domain.common.IndexWriter
import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.domain.videos.model.VideoAccessRuleQuery
import com.boclips.search.service.domain.videos.model.AgeRange
import com.boclips.search.service.domain.videos.model.SourceType
import com.boclips.search.service.domain.videos.model.UserQuery
import com.boclips.search.service.domain.videos.model.VideoMetadata
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.infrastructure.videos.VideoIndexReader
import com.boclips.search.service.infrastructure.videos.VideoIndexWriter
import com.boclips.search.service.testsupport.EmbeddedElasticSearchIntegrationTest
import com.boclips.search.service.testsupport.SearchableVideoMetadataFactory
import com.boclips.search.service.testsupport.TestFactories.createSubjectMetadata
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import java.util.stream.Stream

class SearchServiceProvider : ArgumentsProvider {
    override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {
        val inMemorySearchService = VideoIndexFake()
        val elasticSearchService = VideoIndexReader(EmbeddedElasticSearchIntegrationTest.CLIENT.buildClient())
        val elasticSearchServiceAdmin =
            VideoIndexWriter.createTestInstance(
                EmbeddedElasticSearchIntegrationTest.CLIENT.buildClient(),
                100
            )

        return Stream.of(
            Arguments.of(inMemorySearchService, inMemorySearchService),
            Arguments.of(elasticSearchService, elasticSearchServiceAdmin)
        )
    }
}

class VideoSearchServiceContractTest : EmbeddedElasticSearchIntegrationTest() {
    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `returns empty collection for empty result`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.safeRebuildIndex(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    title = "White Gentleman Dancing"
                )
            )
        )

        val result = queryService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    phrase = "videoQuery that matches nothing",
                    videoAccessRuleQuery = VideoAccessRuleQuery()
                )
            )
        )

        assertThat(result.elements).hasSize(0)
        assertThat(result.counts.totalHits).isEqualTo(0)
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `finds a video matching metadata`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.safeRebuildIndex(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "White Gentleman Dancing"),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    title = "Beer",
                    description = "Behave like a gentleman, cane like a sponge"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    title = "Mixed-race couple playing piano with a dog",
                    description = "Watch and get educated."
                ),
                SearchableVideoMetadataFactory.create(
                    id = "4",
                    title = "Who are you, really?",
                    contentProvider = "Gentleman"
                )
            )
        )

        val result = queryService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    phrase = "gentleman",
                    videoAccessRuleQuery = VideoAccessRuleQuery()
                )
            )
        )

        assertThat(result.elements).containsExactlyInAnyOrder("1", "2", "4")
        assertThat(result.counts.totalHits).isEqualTo(3)
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `filters out videos with excluded content partner ids`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.safeRebuildIndex(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    title = "May Dancing",
                    contentPartnerId = "excluded-cp"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    title = "Trump Dancing",
                    contentPartnerId = "included-cp"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    title = "Johnson Dancing",
                    contentPartnerId = "included-cp"
                )
            )
        )

        val result = queryService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    videoAccessRuleQuery = VideoAccessRuleQuery(
                        excludedContentPartnerIds = setOf("excluded-cp")
                    )
                )
            )
        )

        assertThat(result.elements).containsExactlyInAnyOrder("2", "3")
        assertThat(result.counts.totalHits).isEqualTo(2)
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `searches in transcripts`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.safeRebuildIndex(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", transcript = "the video transcript")
            )
        )

        val result = queryService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    phrase = "video",
                    videoAccessRuleQuery = VideoAccessRuleQuery()
                )
            )
        )

        assertThat(result.elements).containsExactly("1")
        assertThat(result.counts.totalHits).isEqualTo(1)
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `paginates results`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.safeRebuildIndex(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", title = "White Gentleman Dancing"),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    title = "Beer",
                    description = "Behave like a gentleman, cane like a sponge"
                ),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    title = "Mixed-race couple playing piano with a dog",
                    description = "Watch and get educated."
                ),
                SearchableVideoMetadataFactory.create(
                    id = "4",
                    title = "Who are you, really?",
                    contentProvider = "Gentleman"
                )
            )
        )

        val page1 =
            queryService.search(
                PaginatedSearchRequest(
                    query = VideoQuery(
                        phrase = "gentleman", videoAccessRuleQuery = VideoAccessRuleQuery()
                    ), startIndex = 0, windowSize = 2
                )
            )
        val page2 =
            queryService.search(
                PaginatedSearchRequest(
                    query = VideoQuery(
                        phrase = "gentleman",
                        videoAccessRuleQuery = VideoAccessRuleQuery()
                    ), startIndex = 2, windowSize = 2
                )
            )

        assertThat(page1.elements).hasSize(2)
        assertThat(page1.counts.totalHits).isEqualTo(3)

        assertThat(page2.elements).hasSize(1)
        assertThat(page2.counts.totalHits).isEqualTo(3)
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `returns existing ids`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.safeRebuildIndex(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    title = "Beautiful Boy Dancing"
                )
            )
        )

        val query =
            VideoQuery(userQuery = UserQuery(ids = setOf("1", "2", "3", "4")), videoAccessRuleQuery = VideoAccessRuleQuery())
        assertThat(queryService.search(PaginatedSearchRequest(query = query)).counts.totalHits).isEqualTo(1)

        val results = queryService.search(
            PaginatedSearchRequest(
                query = query,
                startIndex = 0,
                windowSize = 2
            )
        )
        assertThat(results.elements).containsExactly("1")
        assertThat(results.counts.totalHits).isEqualTo(1)
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `can filter by source`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "0",
                    description = "Zeroth world war",
                    source = SourceType.BOCLIPS
                ),
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    description = "First world war",
                    source = SourceType.YOUTUBE
                ),
                SearchableVideoMetadataFactory.create(
                    id = "2",
                    description = "Second world war",
                    source = SourceType.BOCLIPS
                ),
                SearchableVideoMetadataFactory.create(
                    id = "3",
                    description = "Third world war",
                    source = SourceType.YOUTUBE
                )
            )
        )

        val results =
            queryService.search(
                PaginatedSearchRequest(
                    query = VideoQuery(
                        "World war",
                        userQuery = UserQuery(
                            source = SourceType.BOCLIPS
                        ),
                        videoAccessRuleQuery = VideoAccessRuleQuery()
                    )
                )
            )

        assertThat(results.elements).containsAll(listOf("0", "2"))
        assertThat(results.elements).doesNotContainAnyElementsOf(listOf("1", "3"))
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `can filter by subject`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "0",
                    title = "TED",
                    subjects = setOf(createSubjectMetadata("subject-one"))
                ),
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    title = "TED",
                    subjects = setOf(createSubjectMetadata("subject-two"))
                )
            )
        )

        val results = queryService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    phrase = "TED",
                    userQuery = UserQuery(
                        subjectIds = setOf("subject-one")
                    ),
                    videoAccessRuleQuery = VideoAccessRuleQuery()
                )
            )
        )

        assertThat(results.elements).containsExactly("0")
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `can filter by promoted`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(
                    id = "0",
                    title = "TED",
                    promoted = false
                ),
                SearchableVideoMetadataFactory.create(
                    id = "1",
                    title = "TED",
                    promoted = true
                )
            )
        )

        val results = queryService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    phrase = "",
                    userQuery = UserQuery(promoted = true),
                    videoAccessRuleQuery = VideoAccessRuleQuery()
                )
            )
        )

        assertThat(results.elements).containsExactly("1")
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `filters by age range`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", ageRangeMin = 3, ageRangeMax = 7),
                SearchableVideoMetadataFactory.create(id = "2", ageRangeMin = 9, ageRangeMax = 11)
            )
        )

        val results = queryService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    userQuery = UserQuery(ageRanges = listOf(AgeRange(3, 7))),
                    videoAccessRuleQuery = VideoAccessRuleQuery()
                )
            )
        )

        assertThat(results.elements).containsExactly("1")
        assertThat(results.counts.totalHits).isEqualTo(1)
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `filters by stream eligibility`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", eligibleForStream = true),
                SearchableVideoMetadataFactory.create(id = "2", eligibleForStream = false)
            )
        )

        val results = queryService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    videoAccessRuleQuery = VideoAccessRuleQuery(isEligibleForStream = true)
                )
            )
        )

        assertThat(results.elements).containsExactly("1")
        assertThat(results.counts.totalHits).isEqualTo(1)
    }

    @ParameterizedTest
    @ArgumentsSource(SearchServiceProvider::class)
    fun `filtering by channel and video ids returns a combination of matches`(
        queryService: IndexReader<VideoMetadata, VideoQuery>,
        adminService: IndexWriter<VideoMetadata>
    ) {
        adminService.upsert(
            sequenceOf(
                SearchableVideoMetadataFactory.create(id = "1", contentPartnerId = "channel-1"),
                SearchableVideoMetadataFactory.create(id = "2", contentPartnerId = "channel-2"),
                SearchableVideoMetadataFactory.create(id = "3", contentPartnerId = "channel-2")
            )
        )

        val results = queryService.search(
            PaginatedSearchRequest(
                query = VideoQuery(
                    videoAccessRuleQuery = VideoAccessRuleQuery(
                        permittedVideoIds = setOf("3"),
                        includedChannelIds = setOf("channel-1")
                    )
                )
            )
        )

        assertThat(results.elements).containsExactlyInAnyOrder("1", "3")
        assertThat(results.counts.totalHits).isEqualTo(2)
    }
}
