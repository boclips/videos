package com.boclips.videos.api.response.video

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.Locale

class LanguageResourceTest {
    @Test
    fun `can convert from Locale to resource`() {
        val locale = Locale("wel")

        val resource = LanguageResource.from(locale)

        assertThat(resource.code).isEqualTo("wel")
        assertThat(resource.displayName).isEqualTo("Welsh")
    }
}
