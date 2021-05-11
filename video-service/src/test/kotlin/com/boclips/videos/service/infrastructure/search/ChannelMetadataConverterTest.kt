package com.boclips.videos.service.infrastructure.search

import com.boclips.contentpartner.service.domain.model.channel.ChannelId
import com.boclips.contentpartner.service.domain.model.channel.ContentType
import com.boclips.contentpartner.service.domain.model.channel.Taxonomy
import com.boclips.search.service.domain.channels.model.CategoryCode
import com.boclips.videos.service.domain.model.suggestions.ChannelSuggestion
import com.boclips.videos.service.testsupport.CategoryWithAncestorsFactory
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class ChannelMetadataConverterTest {

    @Test
    fun `should convert to channel metadata when channel requires video level tagging`() {
        val suggestion = ChannelSuggestion(
            id = ChannelId("some id"),
            name = "channel name",
            eligibleForStream = false,
            contentTypes = listOf(ContentType.INSTRUCTIONAL, ContentType.STOCK, ContentType.NEWS),
            taxonomy = Taxonomy.VideoLevelTagging
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
        Assertions.assertThat(channelMetadata.taxonomy.categories).isNull()
        Assertions.assertThat(channelMetadata.taxonomy.videoLevelTagging).isTrue
    }

    @Test
    fun `should convert to channel metadata when channel has taxonomy categories assigned`() {
        val suggestion = ChannelSuggestion(
            id = ChannelId("some id"),
            name = "channel name",
            eligibleForStream = false,
            contentTypes = listOf(ContentType.INSTRUCTIONAL, ContentType.STOCK, ContentType.NEWS),
            taxonomy = Taxonomy.ChannelLevelTagging(
                categories = setOf(
                    CategoryWithAncestorsFactory.sample(codeValue = "CD"),
                    CategoryWithAncestorsFactory.sample(codeValue = "AB")
                )
            )
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
        Assertions.assertThat(channelMetadata.taxonomy.categories).containsExactlyInAnyOrder(CategoryCode("AB"), CategoryCode("CD"))
        Assertions.assertThat(channelMetadata.taxonomy.videoLevelTagging).isFalse
    }
}
