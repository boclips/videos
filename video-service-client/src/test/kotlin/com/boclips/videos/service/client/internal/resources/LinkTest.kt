package com.boclips.videos.service.client.internal.resources

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class LinkTest {

    @Test
    fun `interpolates link template`() {

        val link = Link()
        link.href = "http://boclips.com/video/{id1}/{id2}"

        val interpolatedLink: Link = link.interpolate(mapOf("id1" to 123, "id2" to 456))

        assertThat(interpolatedLink.href).isEqualTo("http://boclips.com/video/123/456")
    }

    @Test
    fun `interpolates link template with optional hateos params`() {
        val link = Link()
        link.href = "http://boclips.com/v1/blah{?id1,id2,id3}"

        val interpolatedLink: Link = link.interpolate(
            mapOf(
                "id1" to "123",
                "id2" to "456"
            )
        )

        assertThat(interpolatedLink.href).isEqualTo("http://boclips.com/v1/blah?id1=123&id2=456")
    }
}
