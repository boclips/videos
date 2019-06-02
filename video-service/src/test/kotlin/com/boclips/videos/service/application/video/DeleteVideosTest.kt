package com.boclips.videos.service.application.video

import com.boclips.search.service.domain.model.PaginatedSearchRequest
import com.boclips.search.service.domain.videos.model.VideoQuery
import com.boclips.videos.service.application.video.exceptions.VideoNotFoundException
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.service.video.VideoService
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class DeleteVideosTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var deleteVideos: DeleteVideos

    @Autowired
    lateinit var videoService: VideoService

    @Test
    fun `requesting deletion of an existing video deletes the video`() {
        val videoId = saveVideo()

        deleteVideos(videoId.value)

        assertThatThrownBy { videoService.getPlayableVideo(videoId) }
            .isInstanceOf(VideoNotFoundException::class.java)
    }

    @Test
    fun `requesting deletion with blank video ID throws an exception`() {
        assertThatThrownBy { deleteVideos("   ") }
            .isInstanceOf(VideoNotFoundException::class.java)
    }

    @Test
    fun `requesting deletion with null video ID throws an exception`() {
        assertThatThrownBy { deleteVideos(null) }
            .isInstanceOf(VideoNotFoundException::class.java)
    }

    @Test
    fun `remove deletes a video from repository`() {
        val videoId = saveVideo(title = "Some title", description = "test description 3")

        deleteVideos(videoId.value)

        assertThatThrownBy { videoService.getPlayableVideo(videoId) }
            .isInstanceOf(VideoNotFoundException::class.java)
    }

    @Test
    fun `remove deletes a video from search service`() {
        val videoId = saveVideo(title = "Some title", description = "test description 3")

        deleteVideos(videoId.value)

        assertThat(
            fakeVideoSearchService.search(
                PaginatedSearchRequest(
                    query = VideoQuery(
                        "Some title"
                    )
                )
            )
        ).isEmpty()
    }

    @Test
    fun `remove deletes a video from Kaltura`() {
        val videoId = saveVideo(
            title = "Some title",
            description = "test description 3",
            playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "ref-id-123")
        )

        deleteVideos(videoId.value)

        val playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "ref-id-123")
        assertThat(kalturaPlaybackProvider.retrievePlayback(listOf(playbackId))).isEmpty()
    }
}
