package com.boclips.videos.api.request.search

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import javax.validation.Validation
import javax.validation.Validator

class SuggestionsRequestTest {
    private lateinit var validator: Validator

    @BeforeEach
    fun setUp() {
        validator = Validation.buildDefaultValidatorFactory().validator
    }

    @Test
    fun `query must contain 3 characters at least`() {
        assertThat(validator.validate(SuggestionsRequest(query = "abc"))).isEmpty()
        assertThat(validator.validate(SuggestionsRequest(query = "ab")).first().message)
            .isEqualTo("Suggestion must contain between 3 and 20 characters.")
    }

    @Test
    fun `query must contain less than 20 characters`() {
        assertThat(validator.validate(SuggestionsRequest(query = "abcabcabcabcabcabcab"))).isEmpty()
        assertThat(validator.validate(SuggestionsRequest(query = "abcabcabcabcabcabcaba")).first().message)
            .isEqualTo("Suggestion must contain between 3 and 20 characters.")
    }
}
