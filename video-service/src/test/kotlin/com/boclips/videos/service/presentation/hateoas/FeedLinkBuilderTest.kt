package com.boclips.videos.service.presentation.hateoas

import com.boclips.security.testing.setSecurityContext
import com.boclips.videos.service.config.security.UserRoles
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.util.UriComponentsBuilder

class FeedLinkBuilderTest {
    private lateinit var feedLinkBuilder: FeedLinkBuilder

    val uriComponentsBuilderMock = mock<UriComponentsBuilderFactory>()

    @BeforeEach
    fun setUp() {
        whenever(uriComponentsBuilderMock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1"))

        feedLinkBuilder = FeedLinkBuilder(uriComponentsBuilderMock)
    }

    @Test
    fun `returns a video feed link with correct role`() {
        setSecurityContext("matt@boclips.com", roles = arrayOf(UserRoles.VIEW_VIDEOS))
        val videoLink = feedLinkBuilder.videos()!!
        assertThat(videoLink.href).endsWith("/v1/feed/videos{?size}")
        assertThat(videoLink.templated).isTrue
    }

    @Test
    fun `does not return a video feed link when missing role`() {
        val videoLink = feedLinkBuilder.videos()

        assertThat(videoLink).isNull()
    }

    @Test
    fun `next link with valid cursor id`() {
        val link = feedLinkBuilder.nextVideosPage(cursorId = "123", size = 1000)!!
        assertThat(link.href).endsWith("/v1/feed/videos?cursorId=123&size=1000")
        assertThat(link.rel).isEqualTo("next")
        assertThat(link.templated).isFalse
    }

    @Test
    fun `no next link when cursor is null`() {
        val link = feedLinkBuilder.nextVideosPage(cursorId = null, size = 1000)
        assertThat(link).isNull()
    }
}
