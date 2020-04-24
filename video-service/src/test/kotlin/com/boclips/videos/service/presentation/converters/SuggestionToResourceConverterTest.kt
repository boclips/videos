package com.boclips.videos.service.presentation.converters

import com.boclips.videos.service.testsupport.SuggestionFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SuggestionToResourceConverterTest {
    @Test
    fun `convert suggestions`() {
        val suggestions = SuggestionFactory.create(listOf("Ted", "Crash Course Biology"))

        val resource = SuggestionToResourceConverter().convert(suggestions = suggestions)

        assertThat(resource._embedded.suggestions.contentPartners[0].name).isEqualTo("Ted")
        assertThat(resource._embedded.suggestions.contentPartners[1].name).isEqualTo("Crash Course Biology")
    }
}
