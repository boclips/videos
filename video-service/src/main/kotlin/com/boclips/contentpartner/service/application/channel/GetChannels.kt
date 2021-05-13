package com.boclips.contentpartner.service.application.channel

import com.boclips.contentpartner.service.domain.model.channel.Channel
import com.boclips.contentpartner.service.domain.model.channel.ChannelRepository
import com.boclips.search.service.domain.channels.model.SuggestionQuery
import com.boclips.search.service.domain.common.model.SuggestionsSearchRequest
import com.boclips.search.service.infrastructure.channels.ChannelsIndexReader
import com.boclips.videos.api.common.IngestType

class GetChannels(
    private val channelRepository: ChannelRepository,
    private val channelsIndexReader: ChannelsIndexReader
    ) {
    operator fun invoke(
        name: String? = null,
        ingestTypes: List<IngestType>? = null,
        pageSize: Int? = null,
        pageNumber: Int? = null
    ): Iterable<Channel> {
        val filters =
            ChannelFiltersConverter.convert(
                name = name,
                ingestTypes = ingestTypes
            )

        val paginationQuery = if (pageSize != null || pageNumber != null) {
            ChannelPaginationQuery(
                pageSize = pageSize ?: PAGE_SIZE_DEFAULT,
                pageNumber = pageNumber ?: PAGE_NUMBER_DEFAULT
            )
        } else {
            null
        }

        channelsIndexReader.search(
            SuggestionsSearchRequest(
                SuggestionQuery()
            )
        )

        val allChannels = channelRepository.findAll(filters).toList()
        return if (paginationQuery != null) {
            allChannels
                .subList(
                    (paginationQuery.pageNumber - 1) * paginationQuery.pageSize,
                    paginationQuery.pageNumber * paginationQuery.pageSize
                )
        } else {
            allChannels
        }
    }

    companion object {
        const val PAGE_NUMBER_DEFAULT = 1
        const val PAGE_SIZE_DEFAULT = 20
    }
}
