package com.boclips.contentpartner.service.application.channel

import com.boclips.contentpartner.service.common.PageRequest
import com.boclips.contentpartner.service.common.ResultsPage
import com.boclips.contentpartner.service.domain.model.channel.Channel
import com.boclips.contentpartner.service.domain.model.channel.ChannelRequest
import com.boclips.contentpartner.service.domain.model.channel.ChannelSortKey
import com.boclips.contentpartner.service.domain.service.channel.ChannelService
import com.boclips.videos.api.common.IngestType

class GetChannels(private val channelService: ChannelService) {

    companion object {
        const val DEFAULT_PAGE_SIZE = 10000
        const val DEFAULT_PAGE_INDEX = 0
    }

    operator fun invoke(
        name: String? = null,
        ingestTypes: List<IngestType>? = null,
        sortBy: ChannelSortKey? = null,
        categories: List<String>? = emptyList(),
        size: Int? = null,
        page: Int? = null
    ): ResultsPage<Channel> = channelService.search(
        ChannelRequest(
            name = name,
            ingestTypes = ingestTypes,
            sortBy = sortBy,
            categories = categories,
            pageRequest = PageRequest(
                size = size ?: DEFAULT_PAGE_SIZE,
                page = page ?: DEFAULT_PAGE_INDEX
            )
        )
    )
}
