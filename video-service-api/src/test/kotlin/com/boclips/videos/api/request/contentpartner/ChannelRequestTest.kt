package com.boclips.videos.api.request.contentpartner

import com.boclips.videos.api.request.VideoServiceApiFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import javax.validation.Validation
import javax.validation.Validator

class ChannelRequestTest {
    private lateinit var validator: Validator

    @BeforeEach
    fun setUp() {
        val factory = Validation.buildDefaultValidatorFactory()
        validator = factory.validator
    }

    @Test
    fun `validates a valid request`() {
        val validRequest = VideoServiceApiFactory.createChannelRequest()
        val violations = validator.validate(validRequest)
        assertThat(violations).hasSize(0)
    }

    @Test
    fun `invalid name`() {
        val validRequest = VideoServiceApiFactory.createChannelRequest(name = null)
        val violations = validator.validate(validRequest)
        assertThat(violations).hasSize(1)
    }

    @Test
    fun `invalid currency`() {
        val validRequest = VideoServiceApiFactory.createChannelRequest(currency = "not a valid currency")
        val violations = validator.validate(validRequest)
        assertThat(violations).hasSize(1)
    }

    @Test
    fun `blank name`() {
        val validRequest = VideoServiceApiFactory.createChannelRequest(name = "")
        val violations = validator.validate(validRequest)
        assertThat(violations).hasSize(1)
    }
}
