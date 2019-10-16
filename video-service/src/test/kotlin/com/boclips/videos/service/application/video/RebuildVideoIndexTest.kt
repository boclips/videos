package com.boclips.videos.service.application.video

import com.boclips.search.service.domain.common.model.PaginatedSearchRequest
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.search.service.infrastructure.contract.VideoSearchServiceFake
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.model.video.VideoFilter
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.domain.service.video.VideoSearchService
import com.boclips.videos.service.infrastructure.search.DefaultVideoSearch
import com.boclips.videos.service.testsupport.TestFactories
import com.mongodb.MongoClientException
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RebuildVideoIndexTest {

    lateinit var searchService: VideoSearchService

    @BeforeEach
    fun setUp() {
        val inMemorySearchService = VideoSearchServiceFake()
        searchService = DefaultVideoSearch(inMemorySearchService, inMemorySearchService)
    }

    @Test
    fun `execute rebuilds search index`() {
        val videoId1 = TestFactories.aValidId()
        val videoId2 = TestFactories.aValidId()
        val videoId3 = TestFactories.aValidId()

        searchService.upsert(sequenceOf(TestFactories.createVideo(videoId = videoId1)))

        val videoRepository = mock<VideoRepository> {
            on {
                streamAll(eq(VideoFilter.IsStreamable), any())
            } doAnswer { invocations ->
                val consumer = invocations.getArgument(1) as (Sequence<Video>) -> Unit

                consumer(
                    sequenceOf(
                        TestFactories.createVideo(videoId = videoId2),
                        TestFactories.createVideo(videoId = videoId3)
                    )
                )
            }
        }

        val rebuildSearchIndex = RebuildVideoIndex(videoRepository, searchService)

        assertThat(rebuildSearchIndex()).isCompleted.hasNotFailed()

        val searchRequest = PaginatedSearchRequest(
            VideoQuery(
                ids = listOf(
                    videoId1,
                    videoId2,
                    videoId3
                )
            )
        )
        assertThat(searchService.search(searchRequest)).containsExactlyInAnyOrder(videoId2, videoId3)
    }

    @Test
    fun `the future surfaces any underlying exceptions`() {
        val videoRepository = mock<VideoRepository> {
            on {
                streamAll(any(), any())
            } doThrow (MongoClientException("Boom"))
        }

        val rebuildSearchIndex = RebuildVideoIndex(videoRepository, searchService)

        assertThat(rebuildSearchIndex()).hasFailedWithThrowableThat().hasMessage("Boom")
    }
}
