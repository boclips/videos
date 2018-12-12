package com.boclips.videos.service.domain.model.asset

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test


class VideoTypeTest {

    @Test
    fun `parses valid type id to the correct instance`() {
        assertThat(VideoType.fromId(1)).isEqualTo(VideoType.NEWS)
    }

    @Test
    fun `invalid type id throws an exception`() {
        assertThatThrownBy { VideoType.fromId(100) }.hasMessage("The type id 100 is invalid")
    }

}