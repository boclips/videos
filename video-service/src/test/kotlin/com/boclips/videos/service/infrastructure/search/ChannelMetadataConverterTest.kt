package com.boclips.videos.service.infrastructure.search

import com.boclips.contentpartner.service.domain.model.channel.*
import com.boclips.contentpartner.service.testsupport.ChannelFactory
import com.boclips.search.service.domain.channels.model.CategoryCode
import com.boclips.videos.service.testsupport.CategoryWithAncestorsFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import com.boclips.videos.service.domain.model.taxonomy.CategoryCode as SearchServiceCategoryCode

class ChannelMetadataConverterTest {

    @Test
    fun `should convert to channel metadata when channel requires video level tagging`() {
        val suggestion = ChannelFactory.createChannel(
            id = ChannelId("some id"),
            name = "channel name",
            contentTypes = listOf(ContentType.INSTRUCTIONAL, ContentType.STOCK, ContentType.NEWS),
            taxonomy = Taxonomy.VideoLevelTagging,
            ingest = YoutubeScrapeIngest(listOf("id-123"))
        )
        val channelMetadata = ChannelMetadataConverter.convert(suggestion)

        assertThat(channelMetadata.id).isEqualTo("some id")
        assertThat(channelMetadata.name).isEqualTo("channel name")
        assertThat(channelMetadata.eligibleForStream).isFalse
        assertThat(channelMetadata.contentTypes).contains(
            com.boclips.search.service.domain.channels.model.ContentType.NEWS,
            com.boclips.search.service.domain.channels.model.ContentType.STOCK,
            com.boclips.search.service.domain.channels.model.ContentType.INSTRUCTIONAL
        )
        assertThat(channelMetadata.taxonomy.categories).isNull()
        assertThat(channelMetadata.taxonomy.videoLevelTagging).isTrue
        assertThat(channelMetadata.isYoutube).isTrue
    }

    @Test
    fun `should convert to channel metadata when channel has taxonomy categories assigned`() {
        val suggestion = ChannelFactory.createChannel(
            id = ChannelId("some id"),
            name = "channel name",
            contentTypes = listOf(ContentType.INSTRUCTIONAL, ContentType.STOCK, ContentType.NEWS),
            taxonomy = Taxonomy.ChannelLevelTagging(
                categories = setOf(
                    CategoryWithAncestorsFactory.sample(codeValue = "CD", ancestors = setOf(SearchServiceCategoryCode("C"))),
                    CategoryWithAncestorsFactory.sample(codeValue = "AB", ancestors = setOf(SearchServiceCategoryCode("A")))
                )
            ),
            ingest = ManualIngest
        )
        val channelMetadata = ChannelMetadataConverter.convert(suggestion)

        assertThat(channelMetadata.id).isEqualTo("some id")
        assertThat(channelMetadata.name).isEqualTo("channel name")
        assertThat(channelMetadata.eligibleForStream).isFalse
        assertThat(channelMetadata.contentTypes).contains(
            com.boclips.search.service.domain.channels.model.ContentType.NEWS,
            com.boclips.search.service.domain.channels.model.ContentType.STOCK,
            com.boclips.search.service.domain.channels.model.ContentType.INSTRUCTIONAL
        )
        assertThat(channelMetadata.taxonomy.categories)
            .containsExactlyInAnyOrder(CategoryCode("AB"), CategoryCode("CD"))
        assertThat(channelMetadata.taxonomy.categoriesWithAncestors)
            .containsExactlyInAnyOrder(CategoryCode("A"), CategoryCode("AB"), CategoryCode("C"), CategoryCode("CD"))
        assertThat(channelMetadata.taxonomy.videoLevelTagging).isFalse
        assertThat(channelMetadata.isYoutube).isFalse
    }
}
