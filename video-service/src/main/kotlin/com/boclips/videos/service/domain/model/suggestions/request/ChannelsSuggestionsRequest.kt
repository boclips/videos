package com.boclips.videos.service.domain.model.suggestions.request

import com.boclips.search.service.domain.channels.model.ChannelQuery
import com.boclips.videos.service.domain.model.video.VideoAccess
import com.boclips.videos.service.domain.model.video.request.AccessRuleQueryConverter

class ChannelsSuggestionsRequest(
    val text: String,
) {
    fun toQuery(videoAccess: VideoAccess): ChannelQuery {
        return ChannelQuery(
            phrase = text,
            accessRuleQuery = AccessRuleQueryConverter.fromAccessRules(videoAccess)
        )
    }
}
