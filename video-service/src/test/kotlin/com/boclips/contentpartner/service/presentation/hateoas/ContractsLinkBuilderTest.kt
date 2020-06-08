package com.boclips.contentpartner.service.presentation.hateoas

import com.boclips.security.testing.setSecurityContext
import com.boclips.videos.service.config.security.UserRoles
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.util.UriComponentsBuilder

class ContractsLinkBuilderTest {
    private lateinit var contractsLinkBuilder: ContractsLinkBuilder

    @BeforeEach
    fun setUp() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1"))

        contractsLinkBuilder = ContractsLinkBuilder(mock)
    }

    @Test
    fun `fetch all link`() {
        setSecurityContext("back@office.boclips", UserRoles.VIEW_CONTENT_PARTNER_CONTRACTS)

        val link = contractsLinkBuilder.contentPartnerContractsLink()!!

        assertThat(link.href).endsWith("/v1/content-partner-contracts{?size,page}")
        assertThat(link.rel.value()).isEqualTo(ContractsLinkBuilder.Rels.CONTENT_PARTNER_CONTRACTS)
        assertThat(link.isTemplated).isEqualTo(true)
    }

    @Test
    fun `fetch all link when unauthenticated`() {
        setSecurityContext("back@office.boclips")

        val link = contractsLinkBuilder.contentPartnerContractsLink()

        assertThat(link).isNull()
    }
}
