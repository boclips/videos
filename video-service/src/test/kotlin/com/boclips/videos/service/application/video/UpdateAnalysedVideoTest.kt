package com.boclips.videos.service.application.video

import com.boclips.videos.service.domain.model.asset.VideoAssetRepository
import com.boclips.videos.service.domain.model.playback.PlaybackRepository
import com.boclips.videos.service.testsupport.TestFactories
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UpdateAnalysedVideoTest {
    lateinit var playbackRepository: PlaybackRepository
    lateinit var videoAssetRepository: VideoAssetRepository
    lateinit var updateAnalysedVideo: UpdateAnalysedVideo

    @BeforeEach
    fun setUp() {
        playbackRepository = mock()
        videoAssetRepository = mock()
        updateAnalysedVideo = UpdateAnalysedVideo(playbackRepository, videoAssetRepository)
    }

    @Test
    fun `handles exceptions in video lookups`() {
        whenever(videoAssetRepository.find(any())).thenThrow(RuntimeException::class.java)

        assertDoesNotThrow {
            updateAnalysedVideo(TestFactories.createVideoAnalysed())
        }
    }

    @Test
    fun `handles exceptions in caption uploads`() {
        whenever(videoAssetRepository.find(any())).thenReturn(TestFactories.createVideoAsset())
        whenever(playbackRepository.uploadCaptions(any(), any())).thenThrow(RuntimeException::class.java)

        assertDoesNotThrow {
            updateAnalysedVideo(TestFactories.createVideoAnalysed())
        }
    }

    @Test
    fun `handles exceptions in metadata updates`() {
        whenever(videoAssetRepository.find(any())).thenReturn(TestFactories.createVideoAsset())
        whenever(videoAssetRepository.bulkUpdate(any())).thenThrow(RuntimeException::class.java)

        assertDoesNotThrow {
            updateAnalysedVideo(TestFactories.createVideoAnalysed())
        }
    }
}
