package com.boclips.videos.service.application.video.search

import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.video.VideoService
import com.nhaarman.mockito_kotlin.argThat
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Test

internal class GetAllVideosByIdTest {

    @Test
    internal fun `dedup ids`() {
        val videoService = mock<VideoService>()
        val getAllVideosById = GetAllVideosById(videoService)

        getAllVideosById(listOf(VideoId("5c542ab85438cdbcb56ddf02"), VideoId("5c542ab85438cdbcb56ddf02")))

        verify(videoService).getPlayableVideo(argThat<List<VideoId>> { size == 1 })
    }
}
