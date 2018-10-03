package com.boclips.videos.service.infrastructure.playback

import com.boclips.kalturaclient.TestKalturaClient
import com.boclips.kalturaclient.media.MediaEntry
import com.boclips.videos.service.application.exceptions.VideoPlaybackNotFound
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.TestFactories.createVideo
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration

class KalturaPlaybackServiceTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var kalturaPlaybackService: KalturaPlaybackService

    @Autowired
    lateinit var kalturaClient: TestKalturaClient

    @Test
    fun `returns playable videos`() {
        val videosWithPlayback = kalturaPlaybackService.getVideosWithPlayback(listOf(createVideo()))

        assertThat(videosWithPlayback).hasSize(1)
        assertThat(videosWithPlayback[0].videoPlayback).isNotNull()
        assertThat(videosWithPlayback[0].isPlayable()).isTrue()
        assertThat(videosWithPlayback[0].videoPlayback!!.streamUrl).isEqualTo("https://stream/mpegdash/video-1.mp4")
        assertThat(videosWithPlayback[0].videoPlayback!!.thumbnailUrl).isEqualTo("https://thumbnail/thumbnail-1.mp4")
        assertThat(videosWithPlayback[0].videoPlayback!!.duration).isEqualTo(Duration.parse("PT1M"))
    }

    @Test
    fun `returns playable video`() {
        val videoWithPlayback = kalturaPlaybackService.getVideoWithPlayback(createVideo())

        assertThat(videoWithPlayback.videoPlayback).isNotNull()
        assertThat(videoWithPlayback.isPlayable()).isTrue()
    }

    @Test
    fun `removes the playback information`() {
        kalturaClient.addMediaEntry(MediaEntry.builder()
                .id("something")
                .referenceId("ref-123")
                .build())

        val video = createVideo(referenceId = "ref-123")
        kalturaPlaybackService.removePlayback(video)

        assertThatThrownBy { kalturaPlaybackService.getVideoWithPlayback(video) }
                .isInstanceOf(VideoPlaybackNotFound::class.java)
    }

}
