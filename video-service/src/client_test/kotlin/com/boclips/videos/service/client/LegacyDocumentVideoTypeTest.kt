package com.boclips.videos.service.client

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import com.boclips.videos.service.domain.model.video.LegacyVideoType as VideoServiceVideoType

class LegacyDocumentVideoTypeTest {

    @Test
    fun `client video types match video service video types`() {
        val allVideoServiceTypes = VideoServiceVideoType.values().map { it.name }
        val allVideoClientTypes = VideoType.values().map { it.name }

        assertThat(allVideoServiceTypes).containsExactlyElementsOf(allVideoClientTypes)
    }
}