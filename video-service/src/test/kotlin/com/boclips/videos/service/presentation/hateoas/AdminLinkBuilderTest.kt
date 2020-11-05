package com.boclips.videos.service.presentation.hateoas

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.util.UriComponentsBuilder
import java.net.URL

class AdminLinkBuilderTest {
    @AfterEach
    fun setUp() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `return next link`() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(
            UriComponentsBuilder.fromHttpUrl(
                "https://localhost/v1/admin/actions/videos_for_content_package/1"
            )
        )
        val adminLinkBuilder = AdminLinkBuilder(mock)

        val link = adminLinkBuilder.nextContentPackage(
            contentPackageId = "1",
            cursorId = "cursor-id",
            size = 3
        )

        val url = URL(link.href)
        assertThat(url).hasProtocol("https")
        assertThat(url).hasHost("localhost")
        assertThat(url).hasPath("/v1/admin/actions/videos_for_content_package/1")
        assertThat(url).hasParameter("size", "3")
        assertThat(url).hasParameter("cursor", "cursor-id")

        assertThat(link.rel).isEqualTo("next")
    }
}
