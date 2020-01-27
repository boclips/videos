package com.boclips.videos.api.request.video

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class LanguageValidatorTest {
    private val languageValidator = LanguageValidator()

    @Test
    fun `no language`() {
        val isValidLanguage = languageValidator.isValid(null, null)
        assertThat(isValidLanguage).isTrue()
    }

    @Test
    fun `valid ISO 639-2 code`() {
        val isValidLanguage = languageValidator.isValid("eng", null)
        assertThat(isValidLanguage).isTrue()
    }

    @Test
    fun `rejects 2 letter codes code`() {
        val isValidLanguage = languageValidator.isValid("it", null)
        assertThat(isValidLanguage).isFalse()
    }

    @Test
    fun `unknown 3 letter code`() {
        val isValidLanguage = languageValidator.isValid("aaa", null)
        assertThat(isValidLanguage).isFalse()
    }


    @Test
    fun `case insensitive`() {
        val isValidLanguage = languageValidator.isValid("CYM", null)
        assertThat(isValidLanguage).isTrue()
    }
}
