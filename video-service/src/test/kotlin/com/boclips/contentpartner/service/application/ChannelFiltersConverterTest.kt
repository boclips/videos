package com.boclips.contentpartner.service.application

import com.boclips.contentpartner.service.application.channel.ChannelFiltersConverter
import com.boclips.contentpartner.service.domain.model.channel.ChannelFilter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ChannelFiltersConverterTest {
    @Test
    fun `creates a name filter if present`() {
        val filters = ChannelFiltersConverter.convert(name = "hello")

        assertThat(filters).containsExactly(ChannelFilter.NameFilter(name = "hello"))
    }
}

