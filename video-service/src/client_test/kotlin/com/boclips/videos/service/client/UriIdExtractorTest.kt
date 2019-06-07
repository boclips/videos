package com.boclips.videos.service.client

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.net.URI

class UriIdExtractorTest {

    @Test
    fun extractId() {
        val uri = URI.create("https://video-service.boclips.com/v1/videos/123")

        val id = UriIdExtractor.extractId(uri)

        assertThat(id).isEqualTo("123")
    }
}
