package com.boclips.videos.service.application.video.search

import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.service.VideoService
import com.nhaarman.mockito_kotlin.argThat
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class GetAllVideosByIdTest {

    @Test
    internal fun `dedup ids`() {
        val videoService = mock<VideoService>()
        val getAllVideosById = GetAllVideosById(videoService, mock())

        getAllVideosById(listOf(AssetId("5c542ab85438cdbcb56ddf02"), AssetId("5c542ab85438cdbcb56ddf02")))

        verify(videoService).get(argThat<List<AssetId>> { size == 1 })
    }
}