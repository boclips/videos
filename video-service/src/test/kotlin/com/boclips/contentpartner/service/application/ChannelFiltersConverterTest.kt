package com.boclips.contentpartner.service.application

import com.boclips.contentpartner.service.application.channel.ChannelFiltersConverter
import com.boclips.contentpartner.service.domain.model.channel.ChannelFilter
import com.boclips.videos.api.common.IngestType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ChannelFiltersConverterTest {
    @Test
    fun `creates a name filter if present`() {
        val filters = ChannelFiltersConverter.convert(name = "hello")

        assertThat(filters).containsExactly(ChannelFilter.NameFilter(name = "hello"))
    }

    @Test
    fun `creates a ingest type filter if present`() {
        val filters = ChannelFiltersConverter.convert(ingestTypes = listOf(IngestType.MANUAL))

        assertThat(filters).containsExactly(ChannelFilter.IngestTypesFilter(ingestTypes = listOf(IngestType.MANUAL)))
    }

    @Test
    fun `creates a visibility filter if present`() {
        val filters = ChannelFiltersConverter.convert(private = true)

        assertThat(filters).containsExactly(ChannelFilter.PrivateFilter(private = true))
    }
}

