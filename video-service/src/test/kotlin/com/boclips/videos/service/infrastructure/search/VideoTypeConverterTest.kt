package com.boclips.videos.service.infrastructure.search

import com.boclips.search.service.domain.videos.model.VideoType
import com.boclips.videos.service.domain.model.video.LegacyVideoType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class VideoTypeConverterTest {

    @Test
    fun `convert maps legacy type to search service type`() {
        assertThat(VideoTypeConverter.convert(LegacyVideoType.INSTRUCTIONAL_CLIPS)).isEqualTo(VideoType.INSTRUCTIONAL)
        assertThat(VideoTypeConverter.convert(LegacyVideoType.NEWS)).isEqualTo(VideoType.NEWS)
        assertThat(VideoTypeConverter.convert(LegacyVideoType.STOCK)).isEqualTo(VideoType.STOCK)
    }

}
