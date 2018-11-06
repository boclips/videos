package com.boclips.videos.service.application.video

import com.boclips.videos.service.application.video.exceptions.VideoNotFoundException
import com.boclips.videos.service.application.video.exceptions.VideoPlaybackNotFound
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProvider
import com.boclips.videos.service.presentation.video.playback.StreamPlaybackResource
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired

class GetVideosTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var getVideos: GetVideos

    @Test
    fun `getting a single video returns the video with playback information if present`() {
        saveVideo(videoId = 1)

        val video = getVideos.execute("1")

        assertThat(video).isNotNull
        assertThat(video.id).isEqualTo("1")

        val streamPlaybackResource = video.playback as StreamPlaybackResource
        assertThat(streamPlaybackResource.streamUrl).isEqualTo("https://stream/mpegdash/video-1.mp4")
        assertThat(streamPlaybackResource.thumbnailUrl).isEqualTo("https://thumbnail/thumbnail-1.mp4")
    }

    @Test
    fun `getting a single video throws if no playback information if present`() {
        saveVideo(videoId = 123, playbackId = PlaybackId(playbackId = "1111", playbackProvider = PlaybackProvider.KALTURA))

        assertThatThrownBy { getVideos.execute("123") }.isInstanceOf(VideoPlaybackNotFound::class.java)
    }

    @Test
    fun `getting a single video that does not exist throws`() {
        assertThatThrownBy { getVideos.execute("123") }.isInstanceOf(VideoNotFoundException::class.java)
    }

}
