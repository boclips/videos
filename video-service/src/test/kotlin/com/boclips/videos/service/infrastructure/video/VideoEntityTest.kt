package com.boclips.videos.service.infrastructure.video

import com.boclips.videos.service.domain.model.asset.LegacyVideoType
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Duration

class VideoEntityTest {

    @Test
    fun `toVideo returns a video with a correct type`() {
        assertThat(TestFactories.createVideoEntity(typeId = 0).toVideoAsset().type).isEqualTo(LegacyVideoType.OTHER)
        assertThat(TestFactories.createVideoEntity(typeId = 1).toVideoAsset().type).isEqualTo(LegacyVideoType.NEWS)
        assertThat(TestFactories.createVideoEntity(typeId = 2).toVideoAsset().type).isEqualTo(LegacyVideoType.STOCK)
        assertThat(TestFactories.createVideoEntity(typeId = 3).toVideoAsset().type).isEqualTo(LegacyVideoType.INSTRUCTIONAL_CLIPS)
        assertThat(TestFactories.createVideoEntity(typeId = 4).toVideoAsset().type).isEqualTo(LegacyVideoType.TV_CLIPS)
        assertThat(TestFactories.createVideoEntity(typeId = 5).toVideoAsset().type).isEqualTo(LegacyVideoType.NEWS_PACKAGE)
        assertThat(TestFactories.createVideoEntity(typeId = 6).toVideoAsset().type).isEqualTo(LegacyVideoType.UGC_NEWS)
        assertThat(TestFactories.createVideoEntity(typeId = 7).toVideoAsset().type).isEqualTo(LegacyVideoType.VR_360_STOCK)
        assertThat(TestFactories.createVideoEntity(typeId = 8).toVideoAsset().type).isEqualTo(LegacyVideoType.VR_360_IMMERSIVE)
        assertThat(TestFactories.createVideoEntity(typeId = 9).toVideoAsset().type).isEqualTo(LegacyVideoType.SHORT_PROGRAMME)
        assertThat(TestFactories.createVideoEntity(typeId = 10).toVideoAsset().type).isEqualTo(LegacyVideoType.TED_TALKS)
        assertThat(TestFactories.createVideoEntity(typeId = 11).toVideoAsset().type).isEqualTo(LegacyVideoType.TED_ED)
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
    fun `toVideo with empty keywords returns a video with empty keyword list`() {
        assertThat(TestFactories.createVideoEntity(keywords = "").toVideoAsset().keywords).isEmpty()
        assertThat(TestFactories.createVideoEntity(keywords = " ").toVideoAsset().keywords).isEmpty()
        assertThat(TestFactories.createVideoEntity(keywords = "\t").toVideoAsset().keywords).isEmpty()
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