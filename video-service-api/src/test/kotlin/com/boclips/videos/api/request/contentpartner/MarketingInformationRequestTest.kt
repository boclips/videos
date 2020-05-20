package com.boclips.videos.api.request.contentpartner

import com.boclips.videos.api.common.ExplicitlyNull
import com.boclips.videos.api.common.Specified
import com.boclips.videos.api.request.channel.MarketingInformationRequest
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.Test

class MarketingInformationRequestTest {
    @Test
    fun `showreel deserializes correctly from value to Specified`() {
        val request = ObjectMapper().readValue(
            "{\"showreel\": \"someurl\"}",
            MarketingInformationRequest::class.java
        )
        assertThat(request.showreel).isEqualTo(Specified("someurl"))
    }

    @Test
    fun `showreel deserializes from explicit null to ExplicitNull`() {
        val request = ObjectMapper().readValue(
            "{\"showreel\": null}",
            MarketingInformationRequest::class.java
        )
        assertThat(request.showreel).isInstanceOf(ExplicitlyNull::class.java)
    }

    @Test
    fun `showreel deserializes from not being present to null`() {
        val request = ObjectMapper().readValue("{}", MarketingInformationRequest::class.java)
        assertThat(request.showreel).isNull()
    }
}
