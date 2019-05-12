package com.boclips.videos.service.application.video

import com.boclips.videos.service.domain.model.playback.PlaybackRepository
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.testsupport.TestFactories
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UpdateAnalysedVideoTest {
    lateinit var playbackRepository: PlaybackRepository
    lateinit var videoRepository: VideoRepository
    lateinit var updateAnalysedVideo: UpdateAnalysedVideo

    @BeforeEach
    fun setUp() {
        playbackRepository = mock()
        videoRepository = mock()
        updateAnalysedVideo = UpdateAnalysedVideo(playbackRepository, videoRepository)
    }

    @Test
    fun `handles exceptions in video lookups`() {
        whenever(videoRepository.find(any())).thenThrow(RuntimeException::class.java)

        assertDoesNotThrow {
            updateAnalysedVideo(TestFactories.createVideoAnalysed())
        }
    }

    @Test
    fun `handles exceptions in caption uploads`() {
        whenever(videoRepository.find(any())).thenReturn(TestFactories.createVideo())
        whenever(playbackRepository.uploadCaptions(any(), any())).thenThrow(RuntimeException::class.java)

        assertDoesNotThrow {
            updateAnalysedVideo(TestFactories.createVideoAnalysed())
        }
    }

    @Test
    fun `handles exceptions in metadata updates`() {
        whenever(videoRepository.find(any())).thenReturn(TestFactories.createVideo())
        whenever(videoRepository.bulkUpdate(any())).thenThrow(RuntimeException::class.java)

        assertDoesNotThrow {
            updateAnalysedVideo(TestFactories.createVideoAnalysed())
        }
    }
}
