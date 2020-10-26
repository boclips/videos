package com.boclips.videos.service.infrastructure.search

import com.boclips.contentpartner.service.domain.model.channel.ChannelId
import com.boclips.contentpartner.service.domain.model.channel.ContentType
import com.boclips.videos.service.domain.model.suggestions.ChannelSuggestion
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class ChannelMetadataConverterTest {

    @Test
    fun `should convert to channel metadata` () {
        val suggestion = ChannelSuggestion(
            id = ChannelId("some id"),
            name = "channel name",
            eligibleForStream = false,
            contentTypes = listOf(ContentType.INSTRUCTIONAL, ContentType.STOCK, ContentType.NEWS)
        )
        val channelMetadata = ChannelMetadataConverter.convert(suggestion)

        Assertions.assertThat(channelMetadata.id).isEqualTo("some id")
        Assertions.assertThat(channelMetadata.name).isEqualTo("channel name")
        Assertions.assertThat(channelMetadata.eligibleForStream).isFalse
        Assertions.assertThat(channelMetadata.contentTypes).contains(
            com.boclips.search.service.domain.channels.model.ContentType.NEWS,
            com.boclips.search.service.domain.channels.model.ContentType.STOCK,
            com.boclips.search.service.domain.channels.model.ContentType.INSTRUCTIONAL
        )
    }
}