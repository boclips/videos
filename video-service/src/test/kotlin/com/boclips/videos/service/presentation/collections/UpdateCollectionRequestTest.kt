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
    fun `validates a 18+ as valid`() {
        val validRequest = TestFactories.createUpdateCollectionRequest(ageRange = AgeRangeRequest(min = 18, max = null))
        val violations = validator.validate(validRequest)
        Assertions.assertThat(violations).hasSize(0)
    }

    @Test
    fun `validates a 3+ as valid`() {
        val validRequest = TestFactories.createUpdateCollectionRequest(ageRange = AgeRangeRequest(min = 3, max = null))
        val violations = validator.validate(validRequest)
        Assertions.assertThat(violations).hasSize(0)
    }

    @Test
    fun `invalidates negative age range`() {
        val validRequest = TestFactories.createUpdateCollectionRequest(ageRange = AgeRangeRequest(min = -1, max = -5))
        val violations = validator.validate(validRequest)
        Assertions.assertThat(violations).hasSize(1)
    }

    @Test
    fun `invalidates unreasonable age range upper bound`() {
        val validRequest = TestFactories.createUpdateCollectionRequest(ageRange = AgeRangeRequest(min = 3, max = 4000))
        val violations = validator.validate(validRequest)
        Assertions.assertThat(violations).hasSize(1)
    }

    @Test
    fun `invalidates unreasonable age range inverted bound`() {
        val validRequest = TestFactories.createUpdateCollectionRequest(ageRange = AgeRangeRequest(min = 4000, max = 3))
        val violations = validator.validate(validRequest)
        Assertions.assertThat(violations).hasSize(1)
    }

    @Test
    fun `invalidates unreasonable age range with plus`() {
        val validRequest =
            TestFactories.createUpdateCollectionRequest(ageRange = AgeRangeRequest(min = 329, max = null))
        val violations = validator.validate(validRequest)
        Assertions.assertThat(violations).hasSize(1)
    }
}
