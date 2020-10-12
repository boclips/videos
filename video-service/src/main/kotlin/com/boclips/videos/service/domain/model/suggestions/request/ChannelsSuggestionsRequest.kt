package com.boclips.videos.service.domain.model.suggestions.request

import com.boclips.search.service.domain.channels.model.ChannelQuery

class ChannelsSuggestionsRequest(
    val text: String,
) {
    fun toQuery(): ChannelQuery {
        return ChannelQuery(
            phrase = text
        )
    }
}
