package com.boclips.videos.service.application.video

import com.boclips.kalturaclient.KalturaClient
import com.boclips.videos.service.application.video.exceptions.VideoNotFoundException
import com.boclips.videos.service.domain.model.VideoId
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProvider
import com.boclips.videos.service.domain.service.VideoService
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired

class DeleteVideosTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var deleteVideos: DeleteVideos

    @Autowired
    lateinit var videoService: VideoService

    @Autowired
    lateinit var kalturaClient: KalturaClient

    @Test
    fun `requesting deletion of an existing video deletes the video from MySQL`() {
        saveVideo(videoId = 123)

        deleteVideos.execute("123")

        assertThatThrownBy { videoService.findVideoBy(VideoId(videoId = "123")) }
                .isInstanceOf(VideoNotFoundException::class.java)
    }

    @Test
    fun `requesting deletion removes the video from searches`() {
        saveVideo(videoId = 123)

        deleteVideos.execute("123")

        assertThat(fakeSearchService.search("irrelevant query"))
                .doesNotContain("123")
    }

    @Test
    fun `requesting deletion removes the video from kaltura`() {
        saveVideo(videoId = 123, playbackId = PlaybackId(playbackId = "ref-123", playbackProvider = PlaybackProvider.KALTURA))
        kalturaClient.createMediaEntry("ref-123")
        assertThat(kalturaClient.getMediaEntriesByReferenceId("ref-123")).isNotEmpty()

        deleteVideos.execute("123")

        assertThat(kalturaClient.getMediaEntriesByReferenceId("ref-123")).isEmpty()
    }

    @Test
    fun `requesting deletion with blank video ID throws an exception`() {
        assertThatThrownBy { deleteVideos.execute("   ") }
                .isInstanceOf(VideoNotFoundException::class.java)
    }

    @Test
    fun `requesting deletion with null video ID throws an exception`() {
        assertThatThrownBy { deleteVideos.execute(null) }
                .isInstanceOf(VideoNotFoundException::class.java)
    }
}
