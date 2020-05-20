package com.boclips.contentpartner.service.application

import com.boclips.contentpartner.service.application.channel.ChannelFiltersConverter
import com.boclips.contentpartner.service.domain.model.channel.ChannelFilter
import com.boclips.contentpartner.service.domain.model.channel.Credit
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ChannelFiltersConverterTest {
    @Test
    fun `creates a name filter if present`() {
        val filters = ChannelFiltersConverter.convert(name = "hello", accreditedYTChannelId = null)

        assertThat(filters).containsExactly(ChannelFilter.NameFilter(name = "hello"))
    }

    @Test
    fun `creates an official filter if present`() {
        val filters = ChannelFiltersConverter.convert(official = false, accreditedYTChannelId = null)

        assertThat(filters).containsExactly(ChannelFilter.OfficialFilter(official = false))
    }

    @Test
    fun `creates an youtube channel id filter if present`() {
        val filters = ChannelFiltersConverter.convert(accreditedYTChannelId = "123")

        assertThat(filters).containsExactly(ChannelFilter.AccreditedTo(credit = Credit.YoutubeCredit(channelId = "123")))
    }

    @Test
    fun `creates a hubspotId filter if present`() {
        val filters = ChannelFiltersConverter.convert(hubspotId = "12345678", accreditedYTChannelId = null)

        assertThat(filters).containsExactly(ChannelFilter.HubspotIdFilter(hubspotId = "12345678"))
    }
}

