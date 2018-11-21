package com.boclips.videos.service.application.video

import com.boclips.search.service.domain.PaginatedSearchRequest
import com.boclips.videos.service.application.video.exceptions.VideoAssetNotFoundException
import com.boclips.videos.service.domain.model.asset.AssetId
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.domain.service.VideoService
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
        saveVideo(videoId = 123)

        deleteVideos.execute("123")

        assertThatThrownBy { videoService.getVideo(AssetId(value = "123")) }
                .isInstanceOf(VideoAssetNotFoundException::class.java)
    }

    @Test
    fun `requesting deletion with blank video ID throws an exception`() {
        assertThatThrownBy { deleteVideos.execute("   ") }
                .isInstanceOf(VideoAssetNotFoundException::class.java)
    }

    @Test
    fun `requesting deletion with null video ID throws an exception`() {
        assertThatThrownBy { deleteVideos.execute(null) }
                .isInstanceOf(VideoAssetNotFoundException::class.java)
    }

    @Test
    fun `remove deletes a video from repository`() {
        val videoId = AssetId(value = "123")
        saveVideo(videoId = videoId.value.toLong(), title = "Some title", description = "test description 3")

        deleteVideos.execute("123")

        assertThatThrownBy { videoService.getVideo(AssetId(value = "123")) }
                .isInstanceOf(VideoAssetNotFoundException::class.java)
    }

    @Test
    fun `remove deletes a video from search service`() {
        val videoId = AssetId(value = "123")
        saveVideo(videoId = videoId.value.toLong(), title = "Some title", description = "test description 3")

        deleteVideos.execute("123")

        assertThat(fakeSearchService.search(PaginatedSearchRequest(query = "Some title"))).isEmpty()
    }

    @Test
    fun `remove deletes a video from Kaltura`() {
        val videoId = AssetId(value = "123")
        saveVideo(videoId = videoId.value.toLong(), title = "Some title", description = "test description 3")

        deleteVideos.execute("123")

        val playbackId = PlaybackId(type = PlaybackProviderType.KALTURA, value = "ref-id-123")
        assertThat(kalturaPlaybackProvider.retrievePlayback(listOf(playbackId))).isEmpty()
    }
}
