package com.boclips.videos.service.application.video

import com.boclips.videos.service.application.video.exceptions.VideoNotFoundException
import com.boclips.videos.service.application.video.exceptions.VideoPlaybackNotFound
import com.boclips.videos.service.domain.model.playback.PlaybackId
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.presentation.video.playback.StreamPlaybackResource
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class GetVideoByIdTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var getVideoById: GetVideoById

    @Test
    fun `video with playback information returned if present`() {
        saveVideo(videoId = 1)

        val video = getVideoById.execute("1")

        assertThat(video).isNotNull
        assertThat(video.id).isEqualTo("1")

        val streamPlaybackResource = video.playback as StreamPlaybackResource
        assertThat(streamPlaybackResource.streamUrl).isEqualTo("https://stream/mpegdash/video-entry-1.mp4")
        assertThat(streamPlaybackResource.thumbnailUrl).isEqualTo("https://thumbnail/thumbnail-entry-1.mp4")
    }

    @Test
    fun `throws if no playback information if present`() {
        saveVideo(videoId = 123, playbackId = PlaybackId(playbackId = "1111", playbackProviderType = PlaybackProviderType.KALTURA))

        fakeKalturaClient.clear()

        assertThatThrownBy { getVideoById.execute("123") }.isInstanceOf(VideoPlaybackNotFound::class.java)
    }

    @Test
    fun `throws if a video does not exist throws`() {
        assertThatThrownBy { getVideoById.execute("123") }.isInstanceOf(VideoNotFoundException::class.java)
    }

}
