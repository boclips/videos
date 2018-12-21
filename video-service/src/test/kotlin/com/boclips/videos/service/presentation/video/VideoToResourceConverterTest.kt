package com.boclips.videos.service.presentation.video

import com.boclips.videos.service.domain.model.asset.Subject
import com.boclips.videos.service.domain.model.asset.VideoType
import com.boclips.videos.service.presentation.video.playback.StreamPlaybackResource
import com.boclips.videos.service.testsupport.TestFactories
import com.boclips.videos.service.testsupport.TestFactories.createVideo
import com.boclips.videos.service.testsupport.TestFactories.createVideoAsset
import com.boclips.videos.service.testsupport.TestFactories.createYoutubePlayback
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Duration

internal class VideoToResourceConverterTest {

    val kalturaVideo = createVideo(
            videoAsset = createVideoAsset(
                    title = "Do what you love",
                    description = "Best bottle slogan",
                    contentProvider = "WeWork",
                    contentPartnerVideoId = "111",
                    subjects = setOf(Subject("Maths")),
                    type = VideoType.TED_TALKS
            ),
            videoPlayback = TestFactories.createKalturaPlayback()
    )

    val youtubeVideo = createVideo(
            videoAsset = createVideoAsset(
                    title = "Do what you love on youtube",
                    description = "Best bottle slogan",
                    contentProvider = "JacekWork",
                    contentPartnerVideoId = "222",
                    subjects = setOf(Subject("Biology")),
                    type = VideoType.OTHER
            ),
            videoPlayback = createYoutubePlayback()
    )

    @Test
    fun `converts a video from Kaltura`() {
        val videoResource = VideoToResourceConverter().convert(kalturaVideo)

        assertThat(videoResource.title).isEqualTo("Do what you love")
        assertThat(videoResource.description).isEqualTo("Best bottle slogan")
        assertThat(videoResource.contentPartner).isEqualTo("WeWork")
        assertThat(videoResource.contentPartnerVideoId).isEqualTo("111")
        assertThat(videoResource.subjects).containsExactly("Maths")
        assertThat(videoResource.type!!.id).isEqualTo(10)
        assertThat(videoResource.type!!.name).isEqualTo("TED Talks")
        assertThat(videoResource.playback!!.type).isEqualTo("STREAM")
        assertThat(videoResource.playback!!.thumbnailUrl).isEqualTo("kaltura-thumbnail")
        assertThat(videoResource.playback!!.duration).isEqualTo(Duration.ofSeconds(11))
        assertThat(videoResource.playback!!.id).isEqualTo("555")
        assertThat((videoResource.playback!! as StreamPlaybackResource).streamUrl).isEqualTo("kaltura-stream")
    }

    @Test
    fun `converts a video from Youtube`() {
        val videoResource = VideoToResourceConverter().convert(youtubeVideo)

        assertThat(videoResource.title).isEqualTo("Do what you love on youtube")
        assertThat(videoResource.description).isEqualTo("Best bottle slogan")
        assertThat(videoResource.contentPartner).isEqualTo("JacekWork")
        assertThat(videoResource.contentPartnerVideoId).isEqualTo("222")
        assertThat(videoResource.subjects).containsExactly("Biology")
        assertThat(videoResource.type!!.id).isEqualTo(0)
        assertThat(videoResource.type!!.name).isEqualTo("Other")
        assertThat(videoResource.playback!!.type).isEqualTo("YOUTUBE")
        assertThat(videoResource.playback!!.thumbnailUrl).isEqualTo("youtube-thumbnail")
        assertThat(videoResource.playback!!.duration).isEqualTo(Duration.ofSeconds(21))
        assertThat(videoResource.playback!!.id).isEqualTo("444")
    }

    @Test
    fun `converts heterogenous video lists`() {
        val resultResource = VideoToResourceConverter()
                .convert(videos = listOf(youtubeVideo, kalturaVideo))

        assertThat(resultResource.map { it.playback!!.type }).containsExactlyInAnyOrder("STREAM", "YOUTUBE")
    }
}