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

class SuggestionLinkBuilderTest {
    lateinit var suggestionLinkBuilder: SuggestionLinkBuilder

    @BeforeEach
    fun setUp() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1"))

        suggestionLinkBuilder = SuggestionLinkBuilder(mock)
    }

    @Test
    fun `suggestions link`() {
        setSecurityContext("bambi", UserRoles.VIEW_VIDEOS)

        Assertions.assertThat(suggestionLinkBuilder.suggestions()?.rel).isEqualTo("suggestions")
        Assertions.assertThat(suggestionLinkBuilder.suggestions()?.href)
            .isEqualTo("https://localhost/v1/suggestions?query={query}")
    }
}
