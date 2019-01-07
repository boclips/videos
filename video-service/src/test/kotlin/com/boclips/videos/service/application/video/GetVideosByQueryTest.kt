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
        assertThatThrownBy {
            getVideosByQuery.execute(
                    query = null,
                    useCase = null,
                    pageNumber = 0,
                    pageSize = 2
            )
        }.isInstanceOf(QueryValidationException::class.java)
    }

    @Test
    fun `throws when page size too big`() {
        assertThatThrownBy {
            getVideosByQuery.execute(query = "query", useCase = null, pageNumber = 0, pageSize = 1000)
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `throws when page size is too small`() {
        assertThatThrownBy {
            getVideosByQuery.execute(query = "query", useCase = null, pageNumber = 0, pageSize = 0)
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `throws page index is smaller than 0`() {
        assertThatThrownBy {
            getVideosByQuery.execute(query = "query", useCase = null, pageNumber = -1, pageSize = 0)
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `returns paginated results`() {
        saveVideo(videoId = 1, title = "a youtube asset", playbackId = PlaybackId(type = PlaybackProviderType.YOUTUBE, value = "you-123"))
        saveVideo(videoId = 2, title = "a youtube asset", playbackId = PlaybackId(type = PlaybackProviderType.YOUTUBE, value = "you-123"))
        saveVideo(videoId = 3, title = "a another asset", playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "you-123"))
        saveVideo(videoId = 4, title = "a youtube asset", playbackId = PlaybackId(type = PlaybackProviderType.YOUTUBE, value = "you-123"))

        val result = getVideosByQuery.execute(query = "youtube", useCase = null, pageNumber = 1, pageSize = 2)

        assertThat(result.videos).hasSize(1)
        assertThat(result.totalVideos).isEqualTo(3)
        assertThat(result.pageNumber).isEqualTo(1)
        assertThat(result.pageSize).isEqualTo(2)
    }

    @Test
    fun `always filters educational content when usecase is classroom`() {
        saveVideo(videoId = 123, title = "banana", isEducational = true)
        saveVideo(videoId = 124, title = "banana", isEducational = false)

        val videos = getVideosByQuery.execute(query = "banana", useCase = "classroom", pageNumber = 0, pageSize = 2)

        assertThat(videos.videos).hasSize(1)
        assertThat(videos.videos.first().id).isEqualTo("123")
    }
}