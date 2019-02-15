package com.boclips.videos.service.application.video

import com.boclips.search.service.domain.PaginatedSearchRequest
import com.boclips.search.service.domain.Query
import com.boclips.search.service.infrastructure.InMemorySearchService
import com.boclips.videos.service.domain.model.asset.VideoAsset
import com.boclips.videos.service.domain.model.asset.VideoAssetRepository
import com.boclips.videos.service.infrastructure.search.VideoAssetSearchService
import com.boclips.videos.service.testsupport.TestFactories
import com.mongodb.MongoClientException
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doAnswer
import com.nhaarman.mockito_kotlin.doThrow
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RebuildSearchIndexTest {

    lateinit var searchService: VideoAssetSearchService

    @BeforeEach
    fun setUp() {
        val inMemorySearchService = InMemorySearchService()
        searchService = VideoAssetSearchService(inMemorySearchService, inMemorySearchService)
    }

    @Test
    fun `execute rebuilds search index`() {
        val videoAssetId1 = TestFactories.aValidId()
        val videoAssetId2 = TestFactories.aValidId()
        val videoAssetId3 = TestFactories.aValidId()

        searchService.upsert(sequenceOf(TestFactories.createVideoAsset(videoId = videoAssetId1)))

        val videoAssetRepository = mock<VideoAssetRepository> {
            on {
                streamAllSearchable(any())
            } doAnswer { invocations ->
                val consumer = invocations.getArgument(0) as (Sequence<VideoAsset>) -> Unit

                consumer(
                    sequenceOf(
                        TestFactories.createVideoAsset(videoId = videoAssetId2),
                        TestFactories.createVideoAsset(videoId = videoAssetId3)
                    )
                )
            }
        }

        val rebuildSearchIndex = RebuildSearchIndex(videoAssetRepository, searchService)

        assertThat(rebuildSearchIndex()).isCompleted.hasNotFailed()

        val searchRequest = PaginatedSearchRequest(Query(ids = listOf(videoAssetId1, videoAssetId2, videoAssetId3)))
        assertThat(searchService.search(searchRequest)).containsExactlyInAnyOrder(videoAssetId2, videoAssetId3)
    }

    @Test
    fun `the future surfaces any underlying exceptions`() {
        val videoAssetRepository = mock<VideoAssetRepository> {
            on {
                streamAllSearchable(any())
            } doThrow(MongoClientException("Boom"))
        }

        val rebuildSearchIndex = RebuildSearchIndex(videoAssetRepository, searchService)

        assertThat(rebuildSearchIndex()).hasFailedWithThrowableThat().hasMessage("Boom")
    }
}