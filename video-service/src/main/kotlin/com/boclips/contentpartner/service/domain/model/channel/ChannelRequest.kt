package com.boclips.contentpartner.service.domain.model.channel

import com.boclips.contentpartner.service.common.PageRequest
import com.boclips.search.service.domain.channels.model.ChannelMetadata
import com.boclips.search.service.domain.channels.model.ChannelQuery
import com.boclips.search.service.domain.common.model.Sort
import com.boclips.search.service.domain.common.model.SortOrder

data class ChannelRequest(
    val sortBy: ChannelSortKey?,
    val pageRequest: PageRequest
) {
    fun toQuery(): ChannelQuery {
        return ChannelQuery(
            sort = when (sortBy) {
                ChannelSortKey.CATEGORIES_ASC -> listOf(
                    Sort.ByField(
                        fieldName = ChannelMetadata::taxonomy,
                        order = SortOrder.ASC
                    )
                )
                ChannelSortKey.CATEGORIES_DESC -> listOf(
                    Sort.ByField(
                        fieldName = ChannelMetadata::taxonomy,
                        order = SortOrder.DESC
                    )
                )
                null -> emptyList()
            }
        )
    }
}