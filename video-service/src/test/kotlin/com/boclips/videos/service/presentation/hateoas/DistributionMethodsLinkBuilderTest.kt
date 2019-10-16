package com.boclips.videos.service.presentation.hateoas

import com.boclips.security.testing.setSecurityContext
import com.boclips.videos.service.config.security.UserRoles
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.util.UriComponentsBuilder

class DistributionMethodsLinkBuilderTest {
    @Test
    fun `returns link when user can view distribution methods`() {
        setSecurityContext("backoffice@boclips.com", UserRoles.VIEW_DISTRIBUTION_METHODS)

        val link = builder.distributionMethods()!!

        assertThat(link.href).isEqualTo("https://localhost/v1/distribution-methods")
        assertThat(link.isTemplated).isFalse()
    }

    @Test
    fun `does not return link when user is not able to view distribution methods`() {
        setSecurityContext("teacher@boclips.com", UserRoles.VIEW_VIDEOS)

        val link = builder.distributionMethods()

        assertThat(link).isNull()
    }

    lateinit var builder: DistributionMethodsLinkBuilder

    @BeforeEach
    fun setUp() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1"))
        builder = DistributionMethodsLinkBuilder(mock)
    }
}
