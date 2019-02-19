package com.boclips.videos.service.domain.service.video

import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.asset.VideoAssetRepository
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.TestFactories.aValidId
import com.nhaarman.mockito_kotlin.any
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`

class VideoAccessServiceTest {
    private lateinit var videoAccessService: VideoAccessService
    private val videoAssetRepositoryMock = Mockito.mock(VideoAssetRepository::class.java)

    @BeforeEach
    internal fun setUp() {
        videoAccessService = VideoAccessService(videoAssetRepositoryMock)
    }

    @Test
    fun `can find out if a video is accessible`() {
        `when`(videoAssetRepositoryMock.find(any()))
                .thenReturn(TestFactories.createVideoAsset(searchable = true))

        val isVideoAccessible = videoAccessService.accessible(AssetId(value = aValidId()))

        assertThat(isVideoAccessible).isEqualTo(true)
    }

    @Test
    fun `returns false if a video is not accessible`() {
        `when`(videoAssetRepositoryMock.find(any()))
                .thenReturn(TestFactories.createVideoAsset(searchable = false))

        val isVideoAccessible = videoAccessService.accessible(AssetId(value = aValidId()))

        assertThat(isVideoAccessible).isEqualTo(false)
    }

}