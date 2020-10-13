package com.boclips.videos.service.application.channels

import com.boclips.contentpartner.service.domain.model.channel.ChannelId
import com.boclips.contentpartner.service.domain.model.channel.ChannelRepository
import com.boclips.contentpartner.service.testsupport.ChannelFactory
import com.boclips.search.service.domain.channels.model.ChannelQuery
import com.boclips.search.service.domain.common.model.SearchRequestWithoutPagination
import com.boclips.search.service.domain.videos.model.AccessRuleQuery
import com.boclips.search.service.infrastructure.contract.ChannelIndexFake
import com.boclips.videos.service.domain.model.suggestions.ChannelSuggestion
import com.boclips.videos.service.domain.service.suggestions.ChannelIndex
import com.boclips.videos.service.infrastructure.search.DefaultChannelSearch
import com.boclips.videos.service.testsupport.TestFactories
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class RebuildChannelsIndexTest {
    lateinit var index: ChannelIndex

    @BeforeEach
    fun setUp() {
        val inMemorySearchService = ChannelIndexFake()
        index = DefaultChannelSearch(inMemorySearchService, inMemorySearchService)
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
            sequenceOf(
                ChannelSuggestion(
                    name = channel1.name,
                    id = channelId1
                )
            )
        )

        val channelRepository = mock<ChannelRepository> {
            on {
                streamAll(any())
            } doAnswer { invocations ->
                val consumer = invocations.getArgument(0) as (Sequence<ChannelSuggestion>) -> Unit

                consumer(
                    sequenceOf(
                        ChannelSuggestion(
                            name = channel2.name,
                            id = channelId2
                        ),
                        ChannelSuggestion(
                            name = channel3.name,
                            id = channelId3
                        )
                    )
                )
            }
        }

        val rebuildSearchIndex = RebuildChannelsIndex(channelRepository, index)

        rebuildSearchIndex()

        val results = index.search(
            SearchRequestWithoutPagination(
                ChannelQuery(
                    phrase = "chan",
                    accessRuleQuery = AccessRuleQuery()
                )
            )
        )

        assertThat(results.elements).hasSize(2)
    }
}
