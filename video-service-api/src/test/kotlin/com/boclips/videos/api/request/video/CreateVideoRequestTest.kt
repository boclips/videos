package com.boclips.videos.api.request.video

import com.boclips.videos.api.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import javax.validation.Validation
import javax.validation.Validator

class CreateVideoRequestTest {

    lateinit var validator: Validator

    @BeforeEach
    fun setUp() {
        val factory = Validation.buildDefaultValidatorFactory()
        validator = factory.validator
    }

    @Test
    fun `successfully validate video`() {
        val violations = validator.validate(TestFactories.createCreateVideoRequest())

        assertThat(violations).hasSize(0)
    }

    @Test
    fun `provider video id must be set`() {
        val violations = validator.validate(TestFactories.createCreateVideoRequest(providerVideoId = null))

        assertThat(violations).hasSize(1)
        assertThat(violations.first().message).isEqualTo("Provider video id is required")
    }

    @Test
    fun `provider id must be set`() {
        val violations = validator.validate(TestFactories.createCreateVideoRequest(providerId = null))

        assertThat(violations).hasSize(1)
        assertThat(violations.first().message).isEqualTo("Provider id is required")
    }

    @Test
    fun `validate title`() {
        val violations = validator.validate(TestFactories.createCreateVideoRequest(title = null))

        assertThat(violations).hasSize(1)
        assertThat(violations.first().message).isEqualTo("A video title is required")
    }

    @Test
    fun `validate description`() {
        val violations = validator.validate(TestFactories.createCreateVideoRequest(description = null))

        assertThat(violations).hasSize(1)
        assertThat(violations.first().message).isEqualTo("A video description is required")
    }

    @Test
    fun `validate keywords`() {
        val violations = validator.validate(TestFactories.createCreateVideoRequest(keywords = null))

        assertThat(violations).hasSize(1)
        assertThat(violations.first().message).isEqualTo("Keywords are required")
    }

    @Test
    fun `validate released on date`() {
        val violations = validator.validate(TestFactories.createCreateVideoRequest(releasedOn = null))

        assertThat(violations).hasSize(1)
        assertThat(violations.first().message).isEqualTo("Released on date is required")
    }

    @Test
    fun `validate video type`() {
        val violations = validator.validate(TestFactories.createCreateVideoRequest(videoType = null))

        assertThat(violations).hasSize(1)
        assertThat(violations.first().message).isEqualTo("Video type is required")
    }
}
