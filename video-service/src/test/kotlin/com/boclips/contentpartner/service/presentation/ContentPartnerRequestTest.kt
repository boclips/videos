package com.boclips.contentpartner.service.presentation

import com.boclips.contentpartner.service.presentation.ageRange.AgeRangeRequest
import com.boclips.contentpartner.service.testsupport.TestFactories
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import javax.validation.Validation
import javax.validation.Validator

class ContentPartnerRequestTest {

    lateinit var validator: Validator

    @BeforeEach
    fun setUp() {
        val factory = Validation.buildDefaultValidatorFactory()
        validator = factory.getValidator()
    }

    @Test
    fun `validates a valid request`() {
        val validRequest = TestFactories.createContentPartnerRequest()
        val violations = validator.validate(validRequest)
        assertThat(violations).hasSize(0)
    }

    @Test
    fun `invalid name`() {
        val validRequest = TestFactories.createContentPartnerRequest(name = null)
        val violations = validator.validate(validRequest)
        assertThat(violations).hasSize(1)
    }

    @Test
    fun `invalid currency`() {
        val validRequest = TestFactories.createContentPartnerRequest(currency = "not a valid currency")
        val violations = validator.validate(validRequest)
        assertThat(violations).hasSize(1)
    }

    @Test
    fun `blank name`() {
        val validRequest = TestFactories.createContentPartnerRequest(name = "")
        val violations = validator.validate(validRequest)
        assertThat(violations).hasSize(1)
    }

    @Test
    fun `invalid age range`() {
        val validRequest = TestFactories.createContentPartnerRequest(
            ageRange = AgeRangeRequest(
                min = -666,
                max = null
            )
        )
        val violations = validator.validate(validRequest)
        assertThat(violations).hasSize(1)
    }
}
