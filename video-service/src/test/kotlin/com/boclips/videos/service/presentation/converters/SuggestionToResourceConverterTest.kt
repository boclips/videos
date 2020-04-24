package com.boclips.videos.service.presentation.converters

import com.boclips.videos.service.testsupport.SuggestionFactory
import com.nhaarman.mockitokotlin2.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SuggestionToResourceConverterTest {
    @Test
    fun `convert suggestions`() {
        val suggestions = SuggestionFactory.create(listOf("Ted", "Crash Course Biology"))

        val resource = SuggestionToResourceConverter(mock()).convert(query = "ted", suggestions = suggestions)

        assertThat(resource.contentPartners[0].name).isEqualTo("Ted")
        assertThat(resource.contentPartners[1].name).isEqualTo("Crash Course Biology")
    }
}
