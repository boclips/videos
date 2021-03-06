package com.boclips.videos.service.application.channels

import com.boclips.contentpartner.service.domain.model.channel.Channel
import com.boclips.contentpartner.service.domain.model.channel.ChannelId
import com.boclips.contentpartner.service.domain.model.channel.ChannelRepository
import com.boclips.contentpartner.service.domain.model.channel.ContentType
import com.boclips.contentpartner.service.domain.model.channel.DistributionMethod
import com.boclips.contentpartner.service.domain.model.channel.Taxonomy
import com.boclips.contentpartner.service.testsupport.ChannelFactory
import com.boclips.search.service.domain.channels.model.ChannelMetadata
import com.boclips.search.service.domain.channels.model.ChannelQuery
import com.boclips.search.service.domain.channels.model.SuggestionAccessRuleQuery
import com.boclips.search.service.domain.channels.model.SuggestionQuery
import com.boclips.search.service.domain.common.model.PaginatedIndexSearchRequest
import com.boclips.search.service.domain.common.model.Sort
import com.boclips.search.service.domain.common.model.SortOrder
import com.boclips.search.service.domain.common.model.SuggestionRequest
import com.boclips.search.service.infrastructure.contract.ChannelIndexFake
import com.boclips.videos.service.domain.service.suggestions.ChannelIndex
import com.boclips.videos.service.infrastructure.search.DefaultChannelSearch
import com.boclips.videos.service.testsupport.CategoryWithAncestorsFactory
import com.boclips.videos.service.testsupport.TestFactories
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class RebuildChannelIndexFakeTest {
    lateinit var index: ChannelIndex

    @BeforeEach
    fun setUp() {
        val inMemorySearchService = ChannelIndexFake()
        index = DefaultChannelSearch(inMemorySearchService, inMemorySearchService, inMemorySearchService)
    }

    @Test
    fun `rebuilds search index`() {
        val channelId1 = ChannelId(TestFactories.aValidId())
        val channelId2 = ChannelId(TestFactories.aValidId())
        val channelId3 = ChannelId(TestFactories.aValidId())

        val channel1 = ChannelFactory.createChannel(id = channelId1, name = "channel name 1")
        val channel2 = ChannelFactory.createChannel(id = channelId2, name = "channel name 2")
        val channel3 = ChannelFactory.createChannel(id = channelId3, name = "channel name 3")

        index.upsert(
            sequenceOf(channel1)
        )

        val channelRepository = mock<ChannelRepository> {
            on {
                streamAll(any())
            } doAnswer { invocations ->
                val consumer = invocations.getArgument(0) as (Sequence<Channel>) -> Unit

                consumer(
                    sequenceOf(channel2, channel3)
                )
            }
        }

        val rebuildSearchIndex = RebuildChannelIndex(channelRepository, index)

        rebuildSearchIndex()

        val results = index.getSuggestions(
            SuggestionRequest(
                SuggestionQuery(
                    phrase = "chan",
                    accessRuleQuery = SuggestionAccessRuleQuery()
                )
            )
        )

        assertThat(results.elements).hasSize(2)
        assertThat(results.elements[0].name).isEqualTo("channel name 2")
        assertThat(results.elements[1].name).isEqualTo("channel name 3")
    }

    @Test
    fun `search applies given filters`() {
        val channelId1 = ChannelId(TestFactories.aValidId())
        val channelId2 = ChannelId(TestFactories.aValidId())
        val channelId3 = ChannelId(TestFactories.aValidId())

        val channel1 = ChannelFactory.createChannel(
            id = channelId1,
            name = "channel name 1",
            distributionMethods = setOf(DistributionMethod.DOWNLOAD),
            contentTypes = listOf(ContentType.NEWS, ContentType.STOCK)
        )
        val channel2 = ChannelFactory.createChannel(
            id = channelId2,
            name = "channel name 2",
            distributionMethods = setOf(DistributionMethod.STREAM),
            contentTypes = listOf(ContentType.STOCK)
        )
        val channel3 = ChannelFactory.createChannel(
            id = channelId3,
            name = "channel name 3",
            distributionMethods = setOf(DistributionMethod.STREAM),
            contentTypes = listOf(ContentType.NEWS, ContentType.INSTRUCTIONAL)
        )

        index.upsert(
            sequenceOf(channel1, channel2, channel3)
        )

        val results = index.getSuggestions(
            SuggestionRequest(
                SuggestionQuery(
                    phrase = "chan",
                    accessRuleQuery = SuggestionAccessRuleQuery(
                        isEligibleForStream = true,
                        excludedTypes = setOf(com.boclips.search.service.domain.channels.model.ContentType.STOCK),
                        includedTypes = setOf(com.boclips.search.service.domain.channels.model.ContentType.NEWS)
                    )
                )
            )
        )

        assertThat(results.elements).hasSize(1)
        assertThat(results.elements[0].id).isEqualTo(channelId3.value)
    }

    @Test
    fun `search applies taxonomy sort`() {
        val channelId1 = ChannelId(TestFactories.aValidId())
        val channelId2 = ChannelId(TestFactories.aValidId())
        val channelId3 = ChannelId(TestFactories.aValidId())

        val channel1 = ChannelFactory.createChannel(
            id = channelId1,
            name = "channel name 1",
            contentTypes = listOf(ContentType.NEWS, ContentType.STOCK),
            taxonomy = Taxonomy.VideoLevelTagging
        )
        val channel2 = ChannelFactory.createChannel(
            id = channelId2,
            name = "channel name 2",
            contentTypes = listOf(ContentType.STOCK),
            taxonomy = Taxonomy.ChannelLevelTagging(categories = emptySet())
        )
        val channel3 = ChannelFactory.createChannel(
            id = channelId3,
            name = "channel name 3",
            contentTypes = listOf(ContentType.NEWS, ContentType.INSTRUCTIONAL),
            taxonomy = Taxonomy.ChannelLevelTagging(categories = setOf(CategoryWithAncestorsFactory.sample("ABC")))
        )

        index.upsert(
            sequenceOf(channel1, channel2, channel3)
        )

        val results = index.search(
            PaginatedIndexSearchRequest(
                ChannelQuery(
                    phrase = "",
                    sort = listOf(Sort.ByField(fieldName = ChannelMetadata::taxonomy, order = SortOrder.ASC))
                )
            )
        )

        assertThat(results.elements).hasSize(3)
        assertThat(results.elements[0]).isEqualTo(channelId2.value)
        assertThat(results.elements[1]).isEqualTo(channelId1.value)
        assertThat(results.elements[2]).isEqualTo(channelId3.value)
    }
}
