package com.boclips.videos.api.request.video

import com.boclips.videos.api.request.VideoServiceApiFactory
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
        val violations = validator.validate(VideoServiceApiFactory.createCreateVideoRequest())

        assertThat(violations).hasSize(0)
    }

    @Test
    fun `provider video id must be set`() {
        val violations = validator.validate(VideoServiceApiFactory.createCreateVideoRequest(providerVideoId = null))

        assertThat(violations).hasSize(1)
        assertThat(violations.first().message).isEqualTo("Provider video id is required")
    }

    @Test
    fun `provider id must be set`() {
        val violations = validator.validate(VideoServiceApiFactory.createCreateVideoRequest(providerId = null))

        assertThat(violations).hasSize(1)
        assertThat(violations.first().message).isEqualTo("Provider id is required")
    }

    @Test
    fun `validate title`() {
        val violations = validator.validate(VideoServiceApiFactory.createCreateVideoRequest(title = null))

        assertThat(violations).hasSize(1)
        assertThat(violations.first().message).isEqualTo("A video title is required")
    }

    @Test
    fun `validate keywords`() {
        val violations = validator.validate(VideoServiceApiFactory.createCreateVideoRequest(keywords = null))

        assertThat(violations).hasSize(1)
        assertThat(violations.first().message).isEqualTo("Keywords are required")
    }

    @Test
    fun `validate released on date`() {
        val violations = validator.validate(VideoServiceApiFactory.createCreateVideoRequest(releasedOn = null))

        assertThat(violations).hasSize(1)
        assertThat(violations.first().message).isEqualTo("Released on date is required")
    }

    @Test
    fun `validate video types`() {
        val violations = validator.validate(VideoServiceApiFactory.createCreateVideoRequest(videoTypes = null))

        assertThat(violations).hasSize(1)
        assertThat(violations.first().message).isEqualTo("Video types are required")
    }

    @Test
    fun `validate language`() {
        val violations = validator.validate(VideoServiceApiFactory.createCreateVideoRequest(language = "fwafaw"))

        assertThat(violations).hasSize(1)
        assertThat(violations.first().message).isEqualTo("Invalid ISO 639-2/T language code")
    }
}
