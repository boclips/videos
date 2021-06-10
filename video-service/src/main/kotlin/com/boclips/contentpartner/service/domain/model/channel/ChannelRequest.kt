package com.boclips.contentpartner.service.domain.model.channel

import com.boclips.contentpartner.service.common.PageRequest
import com.boclips.search.service.domain.channels.model.CategoryCode
import com.boclips.search.service.domain.channels.model.ChannelMetadata
import com.boclips.search.service.domain.channels.model.ChannelQuery
import com.boclips.search.service.domain.channels.model.Taxonomy
import com.boclips.search.service.domain.common.model.Sort
import com.boclips.search.service.domain.common.model.SortOrder
import com.boclips.videos.api.common.IngestType
import com.boclips.search.service.domain.channels.model.IngestType as ChannelSearchIngestType

data class ChannelRequest(
    val name: String? = null,
    val ingestTypes: List<IngestType>? = emptyList(),
    val sortBy: ChannelSortKey?,
    val categories: List<String>? = emptyList(),
    val pageRequest: PageRequest
) {
    fun toQuery(): ChannelQuery {
        return ChannelQuery(
            name = name,
            ingestTypes = ingestTypes?.map {
                when (it) {
                    IngestType.MANUAL -> ChannelSearchIngestType.MANUAL
                    IngestType.CUSTOM -> ChannelSearchIngestType.CUSTOM
                    IngestType.MRSS -> ChannelSearchIngestType.MRSS
                    IngestType.YOUTUBE -> ChannelSearchIngestType.YOUTUBE
                }
            } ?: emptyList(),
            taxonomy = Taxonomy(
                categoriesWithAncestors = categories?.map {
                    CategoryCode(
                        it
                    )
                }?.toSet(),
                videoLevelTagging = false
            ),
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
                ChannelSortKey.NAME_ASC -> listOf(
                    Sort.ByField(
                        fieldName = ChannelMetadata::name,
                        order = SortOrder.ASC
                    )
                )
                ChannelSortKey.NAME_DESC -> listOf(
                    Sort.ByField(
                        fieldName = ChannelMetadata::name,
                        order = SortOrder.DESC
                    )
                )
                ChannelSortKey.YOUTUBE_ASC -> listOf(
                    Sort.ByField(
                        fieldName = ChannelMetadata::isYoutube,
                        order = SortOrder.ASC
                    )
                )
                ChannelSortKey.YOUTUBE_DESC -> listOf(
                    Sort.ByField(
                        fieldName = ChannelMetadata::isYoutube,
                        order = SortOrder.DESC
                    )
                )
                null -> emptyList()
            }
        )
    }
}
