package com.boclips.videos.service.infrastructure.video

import com.boclips.videos.service.domain.model.asset.VideoType
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Duration

class VideoEntityTest {

    @Test
    fun `toVideo returns a video with a correct type`() {
        assertThat(TestFactories.createVideoEntity(typeId = 0).toVideoAsset().type).isEqualTo(VideoType.OTHER)
        assertThat(TestFactories.createVideoEntity(typeId = 1).toVideoAsset().type).isEqualTo(VideoType.NEWS)
        assertThat(TestFactories.createVideoEntity(typeId = 2).toVideoAsset().type).isEqualTo(VideoType.STOCK)
        assertThat(TestFactories.createVideoEntity(typeId = 3).toVideoAsset().type).isEqualTo(VideoType.INSTRUCTIONAL_CLIPS)
        assertThat(TestFactories.createVideoEntity(typeId = 4).toVideoAsset().type).isEqualTo(VideoType.TV_CLIPS)
        assertThat(TestFactories.createVideoEntity(typeId = 5).toVideoAsset().type).isEqualTo(VideoType.NEWS_PACKAGE)
        assertThat(TestFactories.createVideoEntity(typeId = 6).toVideoAsset().type).isEqualTo(VideoType.UGC_NEWS)
        assertThat(TestFactories.createVideoEntity(typeId = 7).toVideoAsset().type).isEqualTo(VideoType.VR_360_STOCK)
        assertThat(TestFactories.createVideoEntity(typeId = 8).toVideoAsset().type).isEqualTo(VideoType.VR_360_IMMERSIVE)
        assertThat(TestFactories.createVideoEntity(typeId = 9).toVideoAsset().type).isEqualTo(VideoType.SHORT_PROGRAMME)
        assertThat(TestFactories.createVideoEntity(typeId = 10).toVideoAsset().type).isEqualTo(VideoType.TED_TALKS)
        assertThat(TestFactories.createVideoEntity(typeId = 11).toVideoAsset().type).isEqualTo(VideoType.TED_ED)
    }

    @Test
    fun `toVideo extracts playback provider and playback id for youtube videos`() {
        val video = TestFactories.createVideoEntity(playbackProvider = "YOUTUBE", playbackId = "y123").toVideoAsset()

        assertThat(video.playbackId.value).isEqualTo("y123")
        assertThat(video.playbackId.type).isEqualTo(PlaybackProviderType.YOUTUBE)
    }

    @Test
    fun `toVideo extracts playback provider and playback id for kaltura videos`() {
        val video = TestFactories.createVideoEntity(playbackProvider = "KALTURA", playbackId = "k123").toVideoAsset()

        assertThat(video.playbackId.value).isEqualTo("k123")
        assertThat(video.playbackId.type).isEqualTo(PlaybackProviderType.KALTURA)
    }

    @Test
    fun `toVideo returns a video with correct keywords`() {
        assertThat(TestFactories.createVideoEntity(keywords = "k1, k2").toVideoAsset().keywords).containsExactly("k1", "k2")
    }

    @Test
    fun `toVideo returns a video with correct duration`() {
        assertThat(TestFactories.createVideoEntity(duration = "01:02:03").toVideoAsset().duration).isEqualTo(Duration.ofHours(1).plusMinutes(2).plusSeconds(3))
    }

    @Test
    fun `toVideo returns a video with zero duration when duration is misformatted`() {
        assertThat(TestFactories.createVideoEntity(duration = "bunny").toVideoAsset().duration).isEqualTo(Duration.ZERO)
    }

    @Test
    fun `toVideo returns a video with empty restrictions when not specified`() {
        assertThat(TestFactories.createVideoEntity(restrictions = null).toVideoAsset().legalRestrictions).isBlank()
        assertThat(TestFactories.createVideoEntity(restrictions = "US only").toVideoAsset().legalRestrictions).isEqualTo("US only")
    }
}