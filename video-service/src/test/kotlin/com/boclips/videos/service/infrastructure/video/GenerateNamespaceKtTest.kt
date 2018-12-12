package com.boclips.videos.service.infrastructure.video

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


class GenerateNamespaceKtTest {

    @Test
    fun `concatenates provider with the id`() {
        assertThat(generateNamespace("AP", "1234")).isEqualTo("AP:1234")
    }

    @Test
    fun `removes whitespaces from provider`() {
        assertThat(generateNamespace("Ted Talks", "1234")).isEqualTo("TedTalks:1234")
    }
}
