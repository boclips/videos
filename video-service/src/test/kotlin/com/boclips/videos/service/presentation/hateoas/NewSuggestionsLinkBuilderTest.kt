package com.boclips.videos.service.presentation.hateoas

import com.boclips.contentpartner.service.presentation.hateoas.UriComponentsBuilderFactory
import com.boclips.security.testing.setSecurityContext
import com.boclips.videos.service.config.security.UserRoles
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.util.UriComponentsBuilder

internal class NewSuggestionsLinkBuilderTest {
    lateinit var newSuggestionsLinkBuilder: NewSuggestionsLinkBuilder

    @BeforeEach
    fun setUp() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1"))

        newSuggestionsLinkBuilder = NewSuggestionsLinkBuilder(mock)
    }

    @Test
    fun `suggestions link`() {
        setSecurityContext("bambi", UserRoles.VIEW_VIDEOS)

        Assertions.assertThat(newSuggestionsLinkBuilder.suggestions()?.rel).isEqualTo("newSuggestions")
        Assertions.assertThat(newSuggestionsLinkBuilder.suggestions()?.href)
            .isEqualTo("https://localhost/v1/new-suggestions?query={query}")
    }
}