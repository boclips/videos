package com.boclips.videos.service.presentation.video

import com.boclips.videos.service.presentation.video.playback.StreamPlaybackResource
import com.boclips.videos.service.presentation.video.playback.YoutubePlaybackResource
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Duration

internal class VideoToResourceConverterTest {

    val youtubeVideo = TestFactories.createVideo(
            title = "Do what you love on youtube",
            description = "Best bottle slogan",
            contentProvider = "JacekWork",
            videoPlayback = TestFactories.createYoutubePlayback()
    )

    val kalturaVideo = TestFactories.createVideo(
            title = "Do what you love",
            description = "Best bottle slogan",
            contentProvider = "WeWork",
            videoPlayback = TestFactories.createKalturaPlayback()
    )

    @Test
    fun `converts a video from Kaltura`() {
        val videoResource = VideoToResourceConverter().convert(kalturaVideo)

        assertThat(videoResource.title).isEqualTo("Do what you love")
        assertThat(videoResource.description).isEqualTo("Best bottle slogan")
        assertThat(videoResource.contentProvider).isEqualTo("WeWork")
        assertThat(videoResource.playback!!.type).isEqualTo("STREAM")
        assertThat(videoResource.playback!!.thumbnailUrl).isEqualTo("kaltura-thumbnail")
        assertThat(videoResource.playback!!.duration).isEqualTo(Duration.ofSeconds(11))
        assertThat((videoResource.playback!! as StreamPlaybackResource).streamUrl).isEqualTo("kaltura-stream")
    }

    @Test
    fun `converts a video from Youtube`() {
        val videoResource = VideoToResourceConverter().convert(youtubeVideo)

        assertThat(videoResource.title).isEqualTo("Do what you love on youtube")
        assertThat(videoResource.description).isEqualTo("Best bottle slogan")
        assertThat(videoResource.contentProvider).isEqualTo("JacekWork")
        assertThat(videoResource.playback!!.type).isEqualTo("YOUTUBE")
        assertThat(videoResource.playback!!.thumbnailUrl).isEqualTo("youtube-thumbnail")
        assertThat(videoResource.playback!!.duration).isEqualTo(Duration.ofSeconds(21))
        assertThat((videoResource.playback!! as YoutubePlaybackResource).youtubeId).isEqualTo("youtube-id")
    }

    @Test
    fun `converts heterogenous video lists`() {
        val videoResources = VideoToResourceConverter().convert(listOf(youtubeVideo, kalturaVideo))

        assertThat(videoResources.map { it.playback!!.type }).containsExactlyInAnyOrder("STREAM", "YOUTUBE")
    }
}