package com.boclips.videos.service.infrastructure.email

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class NoResultsEmailTest {
    @Test
    fun `prints plain text`() {
        val plainTextEmail = NoResultsEmail(
            "Hans",
            "hans@coolcat.com",
            "stupid query",
            "This is some additional information"
        ).toPlainText()

        assertThat(plainTextEmail).isEqualTo(
            """
            Name: Hans
            Email: hans@coolcat.com
            Query: stupid query
            Description: This is some additional information
        """.trimIndent()
        )
    }
}