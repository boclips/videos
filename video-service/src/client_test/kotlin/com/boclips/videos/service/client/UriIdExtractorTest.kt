package com.boclips.videos.service.client

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.IllegalArgumentException
import java.net.URI

class UriIdExtractorTest {

    @Test
    fun `extractId returns id value for video URIs`() {
        val uri = URI.create("https://video-service.boclips.com/v1/videos/507f191e810c19729de860ea")

        val id = UriIdExtractor.extractId(uri)

        assertThat(id).isEqualTo("507f191e810c19729de860ea")
    }

    @Test
    fun `extractId throws on non-video URIs`() {
        val uri = URI.create("https://video-service.boclips.com/v1/videos")

        assertThrows<IllegalArgumentException> {
            UriIdExtractor.extractId(uri)
        }
    }
}
