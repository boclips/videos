package com.boclips.videos.service.presentation.tag

import org.apache.commons.lang3.StringUtils
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import javax.validation.Validation
import javax.validation.Validator

class CreateTagRequestTest {
    lateinit var validator: Validator

    @BeforeEach
    fun setUp() {
        val factory = Validation.buildDefaultValidatorFactory()
        validator = factory.getValidator()
    }

    @Test
    fun `validates name being null`() {
        val violations = validator.validate(CreateTagRequest(name = null))

        Assertions.assertThat(violations).hasSize(1)
        Assertions.assertThat(violations.first().message).isEqualTo("Tag name is required")
    }

    @Test
    fun `validates minimum length of name`() {
        val violations = validator.validate(CreateTagRequest(name = "a"))

        Assertions.assertThat(violations).hasSize(1)
        Assertions.assertThat(violations.first().message).isEqualTo("Tag name must be between 1 and 100 characters")
    }

    @Test
    fun `validates maximum length of name`() {
        val violations = validator.validate(CreateTagRequest(name = StringUtils.repeat("X", 101)))

        Assertions.assertThat(violations).hasSize(1)
        Assertions.assertThat(violations.first().message).isEqualTo("Tag name must be between 1 and 100 characters")
    }
}