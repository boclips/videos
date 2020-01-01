package com.boclips.videos.api.httpclient

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class VideosClientStagingSmokeTest {
    @Test
    fun `get publicly accessible video`() {
        val videosClient = VideoServiceClientFactory.createVideosClient("https://api.staging-boclips.com")
        val resource = videosClient.getVideo("123")

        assertThat(resource.content).isNotNull
        assertThat(resource.links).hasSize(3)
    }
}