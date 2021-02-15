package com.boclips.videos.service.application.video.indexing

import com.boclips.search.service.domain.videos.legacy.LegacyVideoMetadata
import com.boclips.search.service.domain.videos.legacy.LegacyVideoSearchService
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.model.video.Video
import com.boclips.videos.service.domain.service.video.VideoRepository
import com.boclips.videos.service.application.channels.VideoChannelService
import com.boclips.videos.service.testsupport.TestFactories
import com.mongodb.MongoClientException
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RebuildLegacySearchIndexTest {

    lateinit var legacyVideoSearchService: LegacyVideoSearchService
    lateinit var videoChannelService: VideoChannelService

    @BeforeEach
    internal fun setUp() {
        legacyVideoSearchService = mock()
        videoChannelService = mock()
    }

    @Test
    fun `execute rebuild`() {
        val videoRepository = mock<VideoRepository> {
            on {
                streamAll(any())
            } doAnswer { invocations ->
                val consumer = invocations.getArgument(0) as (Sequence<Video>) -> Unit

                consumer(
                    sequenceOf(
                        TestFactories.createVideo(videoId = TestFactories.aValidId()),
                        TestFactories.createVideo(videoId = TestFactories.aValidId())
                    )
                )
            }
        }

        RebuildLegacySearchIndex(
            videoRepository,
            videoChannelService,
            legacyVideoSearchService
        ).invoke()

        verify(legacyVideoSearchService, times(1)).upsert(any(), anyOrNull())
    }

    @Test
    fun `execute ignores videos with an empty title`() {
        val videoRepository = mockvideoRepository(
            videos = sequenceOf(
                TestFactories.createVideo(title = "")
            )
        )
        val rebuildSearchIndex =
            RebuildLegacySearchIndex(
                videoRepository,
                videoChannelService,
                legacyVideoSearchService
            )

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
        val rebuildSearchIndex =
            RebuildLegacySearchIndex(
                videoRepository,
                videoChannelService,
                legacyVideoSearchService
            )

        rebuildSearchIndex()

        assertThat(getUpsertedVideos()).isEmpty()
    }

    @Test
    fun `the future surfaces any underlying exceptions`() {
        val videoRepository = mock<VideoRepository> {
            on {
                streamAll(any())
            } doThrow (MongoClientException("Boom"))
        }

        val rebuildSearchIndex =
            RebuildLegacySearchIndex(
                videoRepository,
                videoChannelService,
                legacyVideoSearchService
            )

        assertThrows<MongoClientException> {
            rebuildSearchIndex()
        }
    }

    private fun mockvideoRepository(videos: Sequence<Video>): VideoRepository {
        return mock {
            on {
                streamAll(any())
            } doAnswer { invocations ->
                val consumer = invocations.getArgument(0) as (Sequence<Video>) -> Unit
                consumer(videos)
            }
        }
    }

    fun getUpsertedVideos(): List<LegacyVideoMetadata> {
        var upsertedVideos: List<LegacyVideoMetadata>?

        argumentCaptor<Sequence<LegacyVideoMetadata>>().apply {
            verify(legacyVideoSearchService).upsert(capture(), anyOrNull())

            upsertedVideos = firstValue.toList()
        }

        return upsertedVideos!!
    }
}
