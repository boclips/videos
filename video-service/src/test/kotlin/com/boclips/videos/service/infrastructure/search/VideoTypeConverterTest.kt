package com.boclips.videos.service.infrastructure.search

import com.boclips.videos.service.domain.model.video.VideoType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import com.boclips.search.service.domain.videos.model.VideoType as SearchVideoType

class VideoTypeConverterTest {

    @Test
    fun `convert maps legacy type to search service type`() {
        assertThat(VideoTypeConverter.convert(VideoType.INSTRUCTIONAL_CLIPS)).isEqualTo(SearchVideoType.INSTRUCTIONAL)
        assertThat(VideoTypeConverter.convert(VideoType.NEWS)).isEqualTo(SearchVideoType.NEWS)
        assertThat(VideoTypeConverter.convert(VideoType.STOCK)).isEqualTo(SearchVideoType.STOCK)
    }

    @Test
    fun `convert maps search service type to legacy type`() {
        assertThat(VideoTypeConverter.convert(SearchVideoType.INSTRUCTIONAL)).isEqualTo(VideoType.INSTRUCTIONAL_CLIPS)
        assertThat(VideoTypeConverter.convert(SearchVideoType.NEWS)).isEqualTo(VideoType.NEWS)
        assertThat(VideoTypeConverter.convert(SearchVideoType.STOCK)).isEqualTo(VideoType.STOCK)
    }
}
