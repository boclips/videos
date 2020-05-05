package com.boclips.videos.service.application.video

import com.boclips.eventbus.EventBus
import com.boclips.videos.service.domain.model.playback.PlaybackRepository
import com.boclips.videos.service.domain.service.video.VideoRepository
import com.boclips.videos.service.domain.service.video.VideoRetrievalService
import com.boclips.videos.service.testsupport.TestFactories
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class VideoAnalysisServiceTest {
    lateinit var playbackRepository: PlaybackRepository
    lateinit var videoRepository: VideoRepository
    lateinit var videoClassificationService: VideoAnalysisService
    lateinit var videoRetrievalService: VideoRetrievalService
    lateinit var eventBus: EventBus

    @BeforeEach
    fun setUp() {
        playbackRepository = mock()
        videoRepository = mock()
        videoRetrievalService = mock()
        eventBus = mock()
        videoClassificationService =
            VideoAnalysisService(videoRepository, eventBus, playbackRepository)
    }

    @Test
    fun `handles exceptions in video lookups`() {
        whenever(videoRepository.find(any())).thenThrow(RuntimeException::class.java)

        assertDoesNotThrow {
            videoClassificationService.videoAnalysed(TestFactories.createVideoAnalysed())
        }
    }

    @Test
    fun `handles exceptions in caption uploads`() {
        whenever(videoRepository.find(any())).thenReturn(TestFactories.createVideo())
        whenever(playbackRepository.uploadCaptions(any(), any())).thenThrow(RuntimeException::class.java)

        assertDoesNotThrow {
            videoClassificationService.videoAnalysed(TestFactories.createVideoAnalysed())
        }
    }

    @Test
    fun `handles exceptions in metadata updates`() {
        whenever(videoRepository.find(any())).thenReturn(TestFactories.createVideo())
        whenever(videoRepository.bulkUpdate(any())).thenThrow(RuntimeException::class.java)

        assertDoesNotThrow {
            videoClassificationService.videoAnalysed(TestFactories.createVideoAnalysed())
        }
    }
}
