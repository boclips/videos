package com.boclips.videos.service.infrastructure.video

import com.boclips.videos.service.domain.model.VideoType
import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

class VideoEntityTest {

    @Test
    fun `toVideo returns a video with a correct type`() {
        assertThat(TestFactories.createVideoEntity(typeId = 0).toVideo().type).isEqualTo(VideoType.OTHER)
        assertThat(TestFactories.createVideoEntity(typeId = 1).toVideo().type).isEqualTo(VideoType.NEWS)
        assertThat(TestFactories.createVideoEntity(typeId = 2).toVideo().type).isEqualTo(VideoType.STOCK)
        assertThat(TestFactories.createVideoEntity(typeId = 3).toVideo().type).isEqualTo(VideoType.INSTRUCTIONAL_CLIPS)
        assertThat(TestFactories.createVideoEntity(typeId = 4).toVideo().type).isEqualTo(VideoType.TV_CLIPS)
        assertThat(TestFactories.createVideoEntity(typeId = 5).toVideo().type).isEqualTo(VideoType.NEWS_PACKAGE)
        assertThat(TestFactories.createVideoEntity(typeId = 6).toVideo().type).isEqualTo(VideoType.UGC_NEWS)
        assertThat(TestFactories.createVideoEntity(typeId = 7).toVideo().type).isEqualTo(VideoType.VR_360_STOCK)
        assertThat(TestFactories.createVideoEntity(typeId = 8).toVideo().type).isEqualTo(VideoType.VR_360_IMMERSIVE)
        assertThat(TestFactories.createVideoEntity(typeId = 9).toVideo().type).isEqualTo(VideoType.SHORT_PROGRAMME)
        assertThat(TestFactories.createVideoEntity(typeId = 10).toVideo().type).isEqualTo(VideoType.TED_TALKS)
        assertThat(TestFactories.createVideoEntity(typeId = 11).toVideo().type).isEqualTo(VideoType.TED_ED)
    }

    @Test
    fun `toVideo throws an exception when type id is unknown`() {
        assertThatThrownBy {
            TestFactories.createVideoEntity(typeId = 12).toVideo()
        }
                .hasMessage("Unknown type_id: 12")
    }

    @Test
    fun `toVideo returns a video with correct keywords`() {
        assertThat(TestFactories.createVideoEntity(keywords = "k1, k2").toVideo().keywords).containsExactly("k1", "k2")
    }
}