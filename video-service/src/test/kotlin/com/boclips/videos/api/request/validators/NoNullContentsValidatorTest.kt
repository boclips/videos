package com.boclips.videos.api.request.validators

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class NoNullContentsValidatorTest {
    private val contentsNullValidator = NoNullContentsValidator()

    @Test
    fun `no nulls`() {
        val isValid = contentsNullValidator.isValid(listOf("123", "abc"), null)
        assertThat(isValid).isTrue()
    }

    @Test
    fun `a null collection is valid`() {
        val isValid = contentsNullValidator.isValid(null, null)
        assertThat(isValid).isTrue()
    }

    @Test
    fun `fails when contains nulls`() {
        val isValid = contentsNullValidator.isValid(listOf("123", null), null)
        assertThat(isValid).isFalse()
    }
}
