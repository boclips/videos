package com.boclips.videos.service.presentation.collections

import com.boclips.videos.service.testsupport.TestFactories
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import javax.validation.Validation
import javax.validation.Validator

class UpdateCollectionRequestTest {
    lateinit var validator: Validator

    @BeforeEach
    fun setUp() {
        val factory = Validation.buildDefaultValidatorFactory()
        validator = factory.getValidator()
    }

    @Test
    fun `validates a valid request`() {
        val validRequest = TestFactories.createUpdateCollectionRequest()
        val violations = validator.validate(validRequest)
        Assertions.assertThat(violations).hasSize(0)
    }

    @Test
    fun `invalidates non integer age range`() {
        val validRequest = TestFactories.createUpdateCollectionRequest(ageRange = "garbage-range")
        val violations = validator.validate(validRequest)
        Assertions.assertThat(violations).hasSize(1)
        Assertions.assertThat(violations.first().message)
            .isEqualTo("Invalid age range. Example: 3-5. Ranges from 0-19.")
    }

    @Test
    fun `invalidates negative age range`() {
        val validRequest = TestFactories.createUpdateCollectionRequest(ageRange = "-1-5")
        val violations = validator.validate(validRequest)
        Assertions.assertThat(violations).hasSize(1)
        Assertions.assertThat(violations.first().message)
            .isEqualTo("Invalid age range. Example: 3-5. Ranges from 0-19.")
    }

    @Test
    fun `invalidates unreasonable age range upper bound`() {
        val validRequest = TestFactories.createUpdateCollectionRequest(ageRange = "3-4000")
        val violations = validator.validate(validRequest)
        Assertions.assertThat(violations).hasSize(1)
        Assertions.assertThat(violations.first().message)
            .isEqualTo("Invalid age range. Example: 3-5. Ranges from 0-19.")
    }

    @Test
    fun `invalidates unreasonable age range inverted bound`() {
        val validRequest = TestFactories.createUpdateCollectionRequest(ageRange = "4000-3")
        val violations = validator.validate(validRequest)
        Assertions.assertThat(violations).hasSize(1)
        Assertions.assertThat(violations.first().message)
            .isEqualTo("Invalid age range. Example: 3-5. Ranges from 0-19.")
    }
}
