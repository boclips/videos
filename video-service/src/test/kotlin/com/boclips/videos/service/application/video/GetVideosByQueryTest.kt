package com.boclips.videos.service.application.video

import com.boclips.videos.service.application.video.exceptions.QueryValidationException
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class GetVideosByQueryTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var getVideosByQuery: GetVideosByQuery

    @Test
    fun `throws exception when query is null`() {
        assertThatThrownBy { getVideosByQuery.execute(null, 0, 2) }.isInstanceOf(QueryValidationException::class.java)
    }

    @Test
    fun `returns paginated results`() {
        saveVideo(videoId = 1, title = "a youtube asset", playbackId = PlaybackId(type = PlaybackProviderType.YOUTUBE, value = "you-123"))
        saveVideo(videoId = 2, title = "a youtube asset", playbackId = PlaybackId(type = PlaybackProviderType.YOUTUBE, value = "you-123"))
        saveVideo(videoId = 3, title = "a another asset", playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "you-123"))
        saveVideo(videoId = 4, title = "a youtube asset", playbackId = PlaybackId(type = PlaybackProviderType.YOUTUBE, value = "you-123"))

        val result = getVideosByQuery.execute(query = "youtube", pageIndex = 1, pageSize = 2)

        assertThat(result.totalVideos).isEqualTo(3)
        assertThat(result.videos).hasSize(1)
    }
}