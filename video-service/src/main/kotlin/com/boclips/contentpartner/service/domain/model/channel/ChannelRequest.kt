package com.boclips.contentpartner.service.domain.model.channel

import com.boclips.contentpartner.service.common.PageRequest
import com.boclips.search.service.domain.channels.model.ChannelMetadata
import com.boclips.search.service.domain.channels.model.ChannelQuery
import com.boclips.search.service.domain.common.model.Sort
import com.boclips.search.service.domain.common.model.SortOrder
import com.boclips.videos.api.common.IngestType
import com.boclips.search.service.domain.channels.model.IngestType as ChannelSearchIngestType

data class ChannelRequest(
    val ingestTypes: List<IngestType>? = emptyList(),
    val sortBy: ChannelSortKey?,
    val pageRequest: PageRequest
) {
    fun toQuery(): ChannelQuery {
        return ChannelQuery(
            ingestTypes = ingestTypes?.map {
                when (it) {
                    IngestType.MANUAL -> ChannelSearchIngestType.MANUAL
                    IngestType.CUSTOM -> ChannelSearchIngestType.CUSTOM
                    IngestType.MRSS -> ChannelSearchIngestType.MRSS
                    IngestType.YOUTUBE -> ChannelSearchIngestType.YOUTUBE
                }
            } ?: emptyList(),
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