package com.boclips.contentpartner.service.application.channel

import com.boclips.contentpartner.service.common.PageInfo
import com.boclips.contentpartner.service.common.PageRequest
import com.boclips.contentpartner.service.common.ResultsPage
import com.boclips.contentpartner.service.domain.model.channel.Channel
import com.boclips.contentpartner.service.domain.model.channel.ChannelRepository
import com.boclips.contentpartner.service.domain.model.channel.ChannelRequest
import com.boclips.contentpartner.service.domain.model.channel.ChannelSortKey
import com.boclips.contentpartner.service.domain.service.channel.ChannelService
import com.boclips.videos.api.common.IngestType

class GetChannels(
    private val channelRepository: ChannelRepository,
    private val channelService: ChannelService
) {

    companion object {
        const val DEFAULT_PAGE_SIZE = 10
        const val DEFAULT_PAGE_INDEX = 0
    }

    operator fun invoke(
        name: String? = null,
        ingestTypes: List<IngestType>? = null,
        sortBy: ChannelSortKey? = null,
        size: Int? = null,
        page: Int? = null
    ): ResultsPage<Channel> {
        val filters =
            ChannelFiltersConverter.convert(
                name = name,
                ingestTypes = ingestTypes
            )

        val shouldUseSearch = (sortBy != null || size != null || page != null)

        return if (shouldUseSearch) {
            channelService.search(
                ChannelRequest(
                    sortBy = sortBy,
                    pageRequest = PageRequest(
                        size = size ?: DEFAULT_PAGE_SIZE,
                        page = page ?: DEFAULT_PAGE_INDEX
                    )
                )
            )
        } else {
            val channels = channelRepository.findAll(filters).toList()
            ResultsPage(
                elements = channelRepository.findAll(filters),
                pageInfo = PageInfo(
                    hasMoreElements = false,
                    totalElements = channels.size.toLong(),
                    pageRequest = PageRequest(
                        size = channels.size,
                        page = 0
                    )
                )
            )
        }
    }
}
