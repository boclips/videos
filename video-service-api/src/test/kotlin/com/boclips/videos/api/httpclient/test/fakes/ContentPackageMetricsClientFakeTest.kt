package com.boclips.videos.api.httpclient.test.fakes

import com.boclips.videos.api.response.video.VideoIdResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ContentPackageMetricsClientFakeTest {
    @Test
    fun `fake returns video ids`() {
        val fake = ContentPackageMetricsClientFake()
        listOf("1", "2", "3")
            .map(::VideoIdResource)
            .forEach(fake::add)
        val videoIds = fake
            .getVideosForContentPackage("some-id")
            ._embedded.videoIds
        assertThat(videoIds).containsExactlyInAnyOrder("1", "2", "3")
    }
}
