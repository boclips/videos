package com.boclips.videos.service.application.video.search

import com.boclips.videos.service.domain.model.video.VideoAccess
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.domain.service.video.VideoService
import com.boclips.videos.service.testsupport.AccessRulesFactory
import com.boclips.videos.service.testsupport.UserFactory
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.Test

internal class GetAllVideosByIdTest {

    @Test
    internal fun `dedup ids`() {
        val videoService = mock<VideoService>()
        val getAllVideosById = GetAllVideosById(videoService)

        getAllVideosById(
            listOf(VideoId("5c542ab85438cdbcb56ddf02"), VideoId("5c542ab85438cdbcb56ddf02")),
            UserFactory.sample(accessRulesSupplier = { AccessRulesFactory.sample(videoAccess = VideoAccess.Everything) })
        )

        verify(videoService).getPlayableVideos(argThat { size == 1 }, eq(VideoAccess.Everything))
    }
}
