package com.boclips.videos.api.response

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class HateoasLinkTest {
    @Test
    fun `is templated link`() {
        assertThat(HateoasLink("http://something.com{?query}", "what").templated).isTrue()
    }

    @Test
    fun `is not templated link`() {
        assertThat(HateoasLink("http://something.com", "what").templated).isFalse()
        assertThat(HateoasLink("http://something.com?query=true", "what").templated).isFalse()
        assertThat(HateoasLink("http://something.com/120/?query=true", "what").templated).isFalse()
    }
}
