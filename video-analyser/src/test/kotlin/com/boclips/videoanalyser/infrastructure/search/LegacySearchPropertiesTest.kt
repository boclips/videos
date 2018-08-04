package com.boclips.videoanalyser.infrastructure.search

import com.boclips.videoanalyser.infrastructure.search.LegacySearchProperties
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

class LegacySearchPropertiesTest {

    @Test
    fun validate_whenBaseUrlNotSpecified_throws() {
        assertThatThrownBy {
            LegacySearchProperties(token = "123").validate()
        }
                .hasMessage("Legacy boclips API base URL not specified. Check application.yml")
    }

    @Test
    fun validate_whenTokenNotSpecified_throws() {
        assertThatThrownBy {
            LegacySearchProperties(baseUrl = "http://www.bioclips.com").validate()
        }
                .hasMessage("Legacy boclips API token not specified. Check application.yml")
    }
}
