package com.boclips.videos.service.client

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import com.boclips.videos.service.domain.model.video.ContentType as VideoServiceVideoType

class LegacyDocumentVideoTypeTest {

    @Test
    fun `client video types match video service video types`() {
        val allVideoServiceTypes = VideoServiceVideoType.values().map { it.name }
        val allVideoClientTypes = VideoType.values().map { it.name }

        assertThat(allVideoServiceTypes).containsExactlyElementsOf(allVideoClientTypes)
    }

    @Test
    fun `get type for video service type`() {
        val id = VideoServiceVideoType.INSTRUCTIONAL_CLIPS.id

        assertThat(VideoType.fromId(id)).isEqualTo(VideoType.INSTRUCTIONAL_CLIPS)
    }

    @Test
    fun `returns other for a bad id`() {
        val id = -5

        assertThat(VideoType.fromId(id)).isEqualTo(VideoType.OTHER)
    }
}
