package com.boclips.contentpartner.service.presentation

import com.boclips.contentpartner.service.presentation.hateoas.LegacyContentPartnerLinkBuilder
import com.boclips.contentpartner.service.presentation.hateoas.UriComponentsBuilderFactory
import com.boclips.security.testing.setSecurityContext
import com.boclips.videos.service.config.security.UserRoles
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.util.UriComponentsBuilder

class LegacyContentPartnerLinkBuilderTest {
    private lateinit var legacyContentPartnerLinkBuilder: LegacyContentPartnerLinkBuilder

    @BeforeEach
    fun setup() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1"))

        legacyContentPartnerLinkBuilder =
            LegacyContentPartnerLinkBuilder(mock)
    }

    @Test
    fun `all content partners link when authenticated`() {
        setSecurityContext("teacher@boclips.com", UserRoles.VIEW_CONTENT_PARTNERS)

        val link = legacyContentPartnerLinkBuilder.contentPartnersLink()!!

        assertThat(link.href).endsWith("/v1/content-partners{?name}")
        assertThat(link.rel.value()).isEqualTo(LegacyContentPartnerLinkBuilder.Rels.CONTENT_PARTNERS)
        assertThat(link.isTemplated).isTrue()
    }
}
