package com.boclips.contentpartner.service.presentation

import com.boclips.contentpartner.service.presentation.hateoas.ChannelLinkBuilder
import com.boclips.contentpartner.service.presentation.hateoas.UriComponentsBuilderFactory
import com.boclips.security.testing.setSecurityContext
import com.boclips.videos.service.config.security.UserRoles
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.util.UriComponentsBuilder

class ChannelLinkBuilderTest {
    private lateinit var channelLinkBuilder: ChannelLinkBuilder

    @BeforeEach
    fun setup() {
        val mock = mock<UriComponentsBuilderFactory>()
        whenever(mock.getInstance()).thenReturn(UriComponentsBuilder.fromHttpUrl("https://localhost/v1"))

        channelLinkBuilder =
            ChannelLinkBuilder(mock)
    }

    @Test
    fun `all channel link when authenticated`() {
        setSecurityContext("teacher@boclips.com", UserRoles.VIEW_CHANNELS)

        val link = channelLinkBuilder.channelsLink()!!

        assertThat(link.href).endsWith("/v1/channels{?name,projection,sort_by,page,size,categories,ingestType*}")
        assertThat(link.rel.value()).isEqualTo(ChannelLinkBuilder.Rels.CHANNELS)
        assertThat(link.isTemplated).isTrue
    }

    @Test
    fun `single channel link when authenticated`() {
        setSecurityContext("teacher@boclips.com", UserRoles.VIEW_CHANNELS)

        val link = channelLinkBuilder.channelLink("123")

        assertThat(link?.href).endsWith("/v1/channels/123")
        assertThat(link?.rel?.value()).isEqualTo(ChannelLinkBuilder.Rels.CHANNEL)
        assertThat(link?.isTemplated).isFalse
    }

    @Test
    fun `single channel null link when authenticated`() {
        setSecurityContext("teacher@boclips.com", UserRoles.VIEW_CHANNELS)

        val link = channelLinkBuilder.channelLink(null)

        assertThat(link?.href).endsWith("/v1/channels/{id}")
        assertThat(link?.rel?.value()).isEqualTo(ChannelLinkBuilder.Rels.CHANNEL)
        assertThat(link?.isTemplated).isTrue
    }
}
