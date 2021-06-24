package com.boclips.contentpartner.service.domain.model.channel

import com.boclips.contentpartner.service.common.PageRequest
import com.boclips.search.service.domain.channels.model.ChannelMetadata
import com.boclips.search.service.domain.common.model.Sort
import com.boclips.search.service.domain.common.model.SortOrder
import com.boclips.videos.service.domain.model.video.VideoAccess
import com.boclips.videos.service.domain.model.video.VideoAccessRule
import com.boclips.videos.service.domain.model.video.channel.ChannelId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ChannelRequestTest {
    @Test
    fun `ascending categories sort key sorts by taxonomy field name`() {
        val query = ChannelRequest(
            sortBy = ChannelSortKey.CATEGORIES_ASC,
            pageRequest = PageRequest(size = 10, page = 0)
        ).toQuery()

        assertThat(query.sort).containsExactly(
            Sort.ByField(
                fieldName = ChannelMetadata::taxonomy,
                order = SortOrder.ASC
            )
        )
    }

    @Test
    fun `descending categories sort key sorts by taxonomy field name`() {
        val query = ChannelRequest(
            sortBy = ChannelSortKey.CATEGORIES_DESC,
            pageRequest = PageRequest(size = 10, page = 0)
        ).toQuery()

        assertThat(query.sort).containsExactly(
            Sort.ByField(
                fieldName = ChannelMetadata::taxonomy,
                order = SortOrder.DESC
            )
        )
    }

    @Test
    fun `no sorting specified is handled`() {
        val query = ChannelRequest(
            sortBy = null,
            pageRequest = PageRequest(size = 10, page = 0)
        ).toQuery()
        assertThat(query.sort).isEmpty()
    }

    @Test
    fun `ascending name sort key sorts by the name field`() {
        val query = ChannelRequest(
            sortBy = ChannelSortKey.NAME_ASC,
            pageRequest = PageRequest(size = 10, page = 0)
        ).toQuery()

        assertThat(query.sort).containsExactly(
            Sort.ByField(
                fieldName = ChannelMetadata::name,
                order = SortOrder.ASC
            )
        )
    }

    @Test
    fun `descending name sort key sorts by the name field`() {
        val query = ChannelRequest(
            sortBy = ChannelSortKey.NAME_DESC,
            pageRequest = PageRequest(size = 10, page = 0)
        ).toQuery()

        assertThat(query.sort).containsExactly(
            Sort.ByField(
                fieldName = ChannelMetadata::name,
                order = SortOrder.DESC
            )
        )
    }

    @Test
    fun `convert private channels access rules to query`() {

        val query = ChannelRequest(
            sortBy = ChannelSortKey.CATEGORIES_ASC,
            pageRequest = PageRequest(size = 10, page = 0)
        ).toQuery(
            videoAccess = VideoAccess.Rules(
                accessRules = listOf(
                    VideoAccessRule.IncludedPrivateChannels(
                        channelIds = setOf(ChannelId("channel-1"), ChannelId("channel-2"))
                    )
                ),
                privateChannels = emptySet()
            )
        )

        assertThat(query.accessRuleQuery?.includedPrivateChannelIds).isEqualTo(
            setOf("channel-1", "channel-2")
        )
    }
}
