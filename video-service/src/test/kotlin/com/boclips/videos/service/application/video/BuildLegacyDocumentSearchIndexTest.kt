package com.boclips.videos.service.application.video

import com.boclips.search.service.domain.legacy.LegacySearchService
import com.boclips.search.service.domain.legacy.LegacyVideoMetadata
import com.boclips.videos.service.domain.model.Video
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.video.VideoFilter
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.testsupport.TestFactories
import com.mongodb.MongoClientException
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.anyOrNull
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.doAnswer
import com.nhaarman.mockito_kotlin.doThrow
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class BuildLegacyDocumentSearchIndexTest {

    lateinit var legacySearchService: LegacySearchService

    @BeforeEach
    internal fun setUp() {
        legacySearchService = mock()
    }

    @Test
    fun `execute builds search index`() {
        val videoId1 = TestFactories.aValidId()
        val videoId2 = TestFactories.aValidId()

        val videoRepository = mockvideoRepository(
            videos = sequenceOf(
                TestFactories.createVideo(
                    videoId = videoId1,
                    title = "a title",
                    keywords = listOf("keyword")
                ),
                TestFactories.createVideo(videoId = videoId2, keywords = listOf("keyword"))
            )
        )
        val rebuildSearchIndex = BuildLegacySearchIndex(videoRepository, legacySearchService)

        assertThat(rebuildSearchIndex()).isCompleted.hasNotFailed()

        val videos = getUpsertedVideos()
        assertThat(videos).hasSize(2)
        assertThat(videos.first().id).isEqualTo(videoId1)
        assertThat(videos.first().title).isEqualTo("a title")
    }

    @Test
    fun `execute ignores videos with an empty title`() {
        val videoRepository = mockvideoRepository(
            videos = sequenceOf(
                TestFactories.createVideo(
                    title = ""
                )
            )
        )
        val rebuildSearchIndex = BuildLegacySearchIndex(videoRepository, legacySearchService)

        rebuildSearchIndex()

        assertThat(getUpsertedVideos()).isEmpty()
    }

    @Test
    fun `execute ignores youtube videos`() {
        val videoRepository = mockvideoRepository(
            videos = sequenceOf(
                TestFactories.createVideo(
                    playback = TestFactories.createYoutubePlayback(
                        playbackId = PlaybackId(PlaybackProviderType.YOUTUBE, "video")
                    )
                )
            )
        )
        val rebuildSearchIndex = BuildLegacySearchIndex(videoRepository, legacySearchService)

        rebuildSearchIndex()

        assertThat(getUpsertedVideos()).isEmpty()
    }

    @Test
    fun `the future surfaces any underlying exceptions`() {
        val videoRepository = mock<VideoRepository> {
            on {
                streamAll(any(), any())
            } doThrow (MongoClientException("Boom"))
        }

        val rebuildSearchIndex = BuildLegacySearchIndex(videoRepository, legacySearchService)

        assertThat(rebuildSearchIndex()).hasFailedWithThrowableThat().hasMessage("Boom")
    }

    private fun mockvideoRepository(videos: Sequence<Video>): VideoRepository {
        return mock {
            on {
                streamAll(eq(VideoFilter.IsSearchable), any())
            } doAnswer { invocations ->
                val consumer = invocations.getArgument(1) as (Sequence<Video>) -> Unit
                consumer(videos)
            }
        }
    }

    fun getUpsertedVideos(): List<LegacyVideoMetadata> {
        var upsertedVideos: List<LegacyVideoMetadata>?

        argumentCaptor<Sequence<LegacyVideoMetadata>>().apply {
            verify(legacySearchService).upsert(capture(), anyOrNull())

            upsertedVideos = firstValue.toList()
        }

        return upsertedVideos!!
    }
}
