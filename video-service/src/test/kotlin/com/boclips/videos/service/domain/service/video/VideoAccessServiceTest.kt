package com.boclips.videos.service.domain.service.video

import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.model.video.VideoRepository
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.TestFactories.aValidId
import com.nhaarman.mockito_kotlin.any
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`

class VideoAccessServiceTest {
    private lateinit var videoAccessService: VideoAccessService
    private val videoRepositoryMock = Mockito.mock(VideoRepository::class.java)

    @BeforeEach
    fun setUp() {
        videoAccessService = VideoAccessService(videoRepositoryMock)
    }

    @Nested
    @DisplayName("check accessibility")
    inner class AccessibilityTests {
        @Test
        fun `returns true if a video is accessible`() {
            `when`(videoRepositoryMock.find(any()))
                .thenReturn(TestFactories.createVideo(searchable = true))

            val isVideoAccessible = videoAccessService.accessible(VideoId(value = aValidId()))

            assertThat(isVideoAccessible).isEqualTo(true)
        }

        @Test
        fun `returns false if a video is not accessible`() {
            `when`(videoRepositoryMock.find(any()))
                .thenReturn(TestFactories.createVideo(searchable = false))

            val isVideoAccessible = videoAccessService.accessible(VideoId(value = aValidId()))

            assertThat(isVideoAccessible).isEqualTo(false)
        }
    }
}