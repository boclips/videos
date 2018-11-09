package com.boclips.videos.service.infrastructure.video

import com.boclips.videos.service.domain.model.VideoType
import com.boclips.videos.service.domain.model.playback.PlaybackProviderType
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class VideoEntityTest {

    @Test
    fun `toVideo returns a video with a correct type`() {
        assertThat(TestFactories.createVideoEntity(typeId = 0).toVideoDetails().type).isEqualTo(VideoType.OTHER)
        assertThat(TestFactories.createVideoEntity(typeId = 1).toVideoDetails().type).isEqualTo(VideoType.NEWS)
        assertThat(TestFactories.createVideoEntity(typeId = 2).toVideoDetails().type).isEqualTo(VideoType.STOCK)
        assertThat(TestFactories.createVideoEntity(typeId = 3).toVideoDetails().type).isEqualTo(VideoType.INSTRUCTIONAL_CLIPS)
        assertThat(TestFactories.createVideoEntity(typeId = 4).toVideoDetails().type).isEqualTo(VideoType.TV_CLIPS)
        assertThat(TestFactories.createVideoEntity(typeId = 5).toVideoDetails().type).isEqualTo(VideoType.NEWS_PACKAGE)
        assertThat(TestFactories.createVideoEntity(typeId = 6).toVideoDetails().type).isEqualTo(VideoType.UGC_NEWS)
        assertThat(TestFactories.createVideoEntity(typeId = 7).toVideoDetails().type).isEqualTo(VideoType.VR_360_STOCK)
        assertThat(TestFactories.createVideoEntity(typeId = 8).toVideoDetails().type).isEqualTo(VideoType.VR_360_IMMERSIVE)
        assertThat(TestFactories.createVideoEntity(typeId = 9).toVideoDetails().type).isEqualTo(VideoType.SHORT_PROGRAMME)
        assertThat(TestFactories.createVideoEntity(typeId = 10).toVideoDetails().type).isEqualTo(VideoType.TED_TALKS)
        assertThat(TestFactories.createVideoEntity(typeId = 11).toVideoDetails().type).isEqualTo(VideoType.TED_ED)
    }

    @Test
    fun `toVideo throws an exception when type id is unknown`() {
        assertThatThrownBy {
            TestFactories.createVideoEntity(typeId = 12).toVideoDetails()
        }
                .hasMessage("Unknown type_id: 12")
    }

    @Test
    fun `toVideo extracts playback provider and playback id for youtube videos`() {
        val video = TestFactories.createVideoEntity(playbackProvider = "YOUTUBE", playbackId = "y123").toVideoDetails()

        assertThat(video.playbackId.value).isEqualTo("y123")
        assertThat(video.playbackId.type).isEqualTo(PlaybackProviderType.YOUTUBE)
    }

    @Test
    fun `toVideo extracts playback provider and playback id for kaltura videos`() {
        val video = TestFactories.createVideoEntity(playbackProvider = "KALTURA", playbackId = "k123").toVideoDetails()

        assertThat(video.playbackId.value).isEqualTo("k123")
        assertThat(video.playbackId.type).isEqualTo(PlaybackProviderType.KALTURA)
    }

    @Test
    fun `toVideo returns a video with correct keywords`() {
        assertThat(TestFactories.createVideoEntity(keywords = "k1, k2").toVideoDetails().keywords).containsExactly("k1", "k2")
    }
}