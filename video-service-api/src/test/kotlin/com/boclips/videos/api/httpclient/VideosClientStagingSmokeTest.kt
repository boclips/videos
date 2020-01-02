package com.boclips.videos.api.httpclient

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class VideosClientStagingSmokeTest {
    @Disabled
    @Test
    fun `get publicly accessible video`() {
        val videosClient = VideosClient.create("https://api.staging-boclips.com")
        val resource = videosClient.getVideo("123")

        assertThat(resource.content).isNotNull
        assertThat(resource.links).hasSize(3)
    }
}
