package com.boclips.videos.service.domain.model.video

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class VideoAccessTest {
    @Test
    fun `to string`() {
        val rule = VideoAccessRule.ExcludedIds(emptySet())
        val videoAccess = VideoAccess.Rules(listOf(rule), emptySet())
        assertThat(videoAccess.toString()).isEqualTo(
            rule.toString()
        )
    }
}
