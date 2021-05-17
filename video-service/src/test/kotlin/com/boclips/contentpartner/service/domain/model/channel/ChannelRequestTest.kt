package com.boclips.contentpartner.service.domain.model.channel

import com.boclips.contentpartner.service.common.PageRequest
import com.boclips.search.service.domain.channels.model.ChannelMetadata
import com.boclips.search.service.domain.common.model.Sort
import com.boclips.search.service.domain.common.model.SortOrder
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
}