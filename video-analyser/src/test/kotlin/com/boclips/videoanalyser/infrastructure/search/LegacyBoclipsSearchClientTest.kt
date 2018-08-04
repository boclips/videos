package com.boclips.videoanalyser.infrastructure.search

import com.boclips.videoanalyser.infrastructure.search.LegacyBoclipsSearchClient
import com.boclips.videoanalyser.infrastructure.search.LegacySearchProperties
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.springframework.boot.web.client.RestTemplateBuilder

class LegacyBoclipsSearchClientTest {

    @Test
    fun searchTop10_propertiesNotValid_throws() {
        assertThatThrownBy {
            LegacyBoclipsSearchClient(
                    RestTemplateBuilder(),
                    LegacySearchProperties()
            ).searchTop10("math")
        }
    }
}
