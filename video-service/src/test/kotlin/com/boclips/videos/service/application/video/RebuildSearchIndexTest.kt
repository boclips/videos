package com.boclips.videos.service.application.video

import com.boclips.search.service.domain.PaginatedSearchRequest
import com.boclips.search.service.domain.Query
import com.boclips.search.service.infrastructure.InMemorySearchService
import com.boclips.videos.service.domain.model.asset.VideoAsset
import com.boclips.videos.service.domain.model.asset.VideoAssetRepository
import com.boclips.videos.service.infrastructure.search.VideoAssetSearchService
import com.boclips.videos.service.testsupport.TestFactories
import com.nhaarman.mockito_kotlin.KStubbing
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doAnswer
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class RebuildSearchIndexTest {

    @Test
    fun `execute rebuilds search index`() {
        val inMemorySearchService = InMemorySearchService()
        val searchService = VideoAssetSearchService(inMemorySearchService, inMemorySearchService)
        searchService.upsert(sequenceOf(TestFactories.createVideoAsset(videoId = "1")))

        val videoAssetRepository = mock<VideoAssetRepository> {
            on {
                streamAll(any())
            } doAnswer { invocations ->
                val consumer = invocations.getArgument(0) as (Sequence<VideoAsset>) -> Unit

                consumer(sequenceOf(
                        TestFactories.createVideoAsset(videoId = "2"),
                        TestFactories.createVideoAsset(videoId = "3")
                ))
            }
        }

        val rebuildSearchIndex = RebuildSearchIndex(videoAssetRepository, searchService)

        rebuildSearchIndex.execute()

        assertThat(searchService.search(PaginatedSearchRequest(Query(ids = listOf("1", "2", "3"))))).containsExactlyInAnyOrder("2", "3")
    }
}