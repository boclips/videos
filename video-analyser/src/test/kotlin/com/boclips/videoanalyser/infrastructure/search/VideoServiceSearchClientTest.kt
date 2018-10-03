package com.boclips.videoanalyser.infrastructure.search

import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.springframework.boot.web.client.RestTemplateBuilder

class VideoServiceSearchClientTest {

    @Test
    fun searchTop10_propertiesNotValid_throws() {
        assertThatThrownBy {
            VideoServiceSearchClient(
                    RestTemplateBuilder(),
                    VideoServiceSearchProperties()
            )
        }
    }

}