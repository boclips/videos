package com.boclips.search.service.domain.common.model

import com.boclips.search.service.infrastructure.channels.ChannelsPagination

data class ChannelsSearchRequest (
    val pagination: ChannelsPagination?
)
