package com.boclips.videos.service.client.v2

import com.boclips.video.service.client.v2.VideoServiceClientFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class VideoServiceClientContractTest {
    @Test
    fun `get publicly accessible video`() {
        val videoServiceClient = VideoServiceClientFactory.create("https://api.staging-boclips.com")

        val resource = videoServiceClient.getVideo("123")

        assertThat(resource.content).isNotNull
        assertThat(resource.links).hasSize(3)
    }

    @Test
    fun `get publicly accessible subjects`() {
        val videoServiceClient = VideoServiceClientFactory.create("https://api.staging-boclips.com")

        val resource = videoServiceClient.subjects()

        assertThat(resource.content).isNotNull
    }
}