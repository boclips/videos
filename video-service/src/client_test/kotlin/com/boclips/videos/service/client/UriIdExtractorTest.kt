package com.boclips.videos.service.client

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.net.URI

class UriIdExtractorTest {

    @Test
    fun `extractId returns id value for video URIs`() {
        val uri = URI.create("https://video-service.boclips.com/v1/videos/507f191e810c19729de860ea")

        val id = UriIdExtractor.extractId(uri, UriIdExtractor.VIDEO_ID_URI_PATTERN)

        assertThat(id).isEqualTo("507f191e810c19729de860ea")
    }

    @Test
    fun `extractId throws on non-video URIs`() {
        val uri = URI.create("https://video-service.boclips.com/v1/videos/garbage")

        assertThrows<IllegalArgumentException> {
            UriIdExtractor.extractId(uri, UriIdExtractor.VIDEO_ID_URI_PATTERN)
        }
    }

    @Test
    fun `extractId handles legacy video ids`() {
        val uri = URI.create("https://video-service.boclips.com/v1/videos/5678")

        val id = UriIdExtractor.extractId(uri, UriIdExtractor.VIDEO_ID_URI_PATTERN)

        assertThat(id).isEqualTo("5678")
    }

    @Test
    fun `extractId returns id value for content partners`() {
        val uri = URI.create("https://video-service.boclips.com/v1/content-partners/507f191e810c19729de860ea")

        val id = UriIdExtractor.extractId(uri, UriIdExtractor.CONTENT_PARTNER_ID_URI_PATTERN)

        assertThat(id).isEqualTo("507f191e810c19729de860ea")
    }
}
