package com.boclips.videos.api.request.contentpartner

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

val currencyCodeValidator = CurrencyCodeValidator()

class CurrencyCodeValidatorTest {

    @Test
    fun `no currency code`() {
        val valid = currencyCodeValidator.isValid(null, null)

        assertThat(valid).isTrue()
    }

    @Test
    fun `valid currency code`() {
        val valid = currencyCodeValidator.isValid("EUR", null)

        assertThat(valid).isTrue()
    }

    @Test
    fun `invalid currency code`() {
        val valid = currencyCodeValidator.isValid("n ot quite valid", null)

        assertThat(valid).isFalse()
    }

    @Test
    fun `unknown currency code`() {
        val valid = currencyCodeValidator.isValid("XYZ", null)

        assertThat(valid).isFalse()
    }
}
