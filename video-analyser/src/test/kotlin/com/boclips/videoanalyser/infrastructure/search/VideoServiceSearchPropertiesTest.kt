package com.boclips.videoanalyser.infrastructure.search

import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

class VideoServiceSearchPropertiesTest {

    val validProperties = VideoServiceSearchProperties(baseUrl = "http://example.com", username = "username", password = "password")

    @Test
    fun validate_whenBaseUrlNotSpecified_throws() {
        assertThatThrownBy {
            validProperties.copy(baseUrl = "").validate()
        }
                .hasMessage("Video service base URL not specified. Check application.yml")
    }

    @Test
    fun validate_whenUsernameNotSpecified_throws() {
        assertThatThrownBy {
            validProperties.copy(username = "").validate()
        }
                .hasMessage("Video service username not specified. Check application.yml")
    }

    @Test
    fun validate_whenPasswordNotSpecified_throws() {
        assertThatThrownBy {
            validProperties.copy(password = "").validate()
        }
                .hasMessage("Video service password not specified. Check application.yml")
    }
}
