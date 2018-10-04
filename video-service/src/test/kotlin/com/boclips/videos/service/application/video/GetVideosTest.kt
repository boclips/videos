package com.boclips.videos.service.application.video

import com.boclips.videos.service.application.exceptions.VideoNotFoundException
import com.boclips.videos.service.application.exceptions.VideoPlaybackNotFound
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
        saveVideo(videoId = 123)

        val video = getVideos.get("123")

        assertThat(video).isNotNull
        assertThat(video.id).isEqualTo("123")
        assertThat(video.streamUrl).isEqualTo("https://stream/mpegdash/video-1.mp4")
        assertThat(video.thumbnailUrl).isEqualTo("https://thumbnail/thumbnail-1.mp4")
    }

    @Test
    fun `getting a single video without reference id returns the video with playback information`() {
        fakeKalturaClient.addMediaEntry(mediaEntry(referenceId = "123"))
        saveVideo(videoId = 123, referenceId = null)

        val video = getVideos.get("123")

        assertThat(video).isNotNull
        assertThat(video.id).isEqualTo("123")
    }

    @Test
    fun `getting a single video throws if no playback information if present`() {
        saveVideo(videoId = 123, referenceId = "1111")

        assertThatThrownBy { getVideos.get("123") }.isInstanceOf(VideoPlaybackNotFound::class.java)
    }

    @Test
    fun `getting a single video that does not exist throws`() {
        assertThatThrownBy { getVideos.get("123") }.isInstanceOf(VideoNotFoundException::class.java)
    }

}
