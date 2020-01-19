package com.boclips.videos.service.infrastructure.video

import com.boclips.videos.service.infrastructure.convertPageToIndex
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ConvertPageToIndexKtTest {

    @Test
    fun `calculates the index end of a page`() {
        assertThat(convertPageToIndex(50, 0)).isEqualTo(0)
        assertThat(convertPageToIndex(50, 1)).isEqualTo(50)
        assertThat(convertPageToIndex(50, 2)).isEqualTo(100)
    }
}
