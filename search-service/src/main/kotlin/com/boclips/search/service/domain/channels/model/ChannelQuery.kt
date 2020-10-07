package com.boclips.search.service.domain.channels.model

import com.boclips.search.service.domain.common.model.SearchQuery

class ChannelQuery (
    phrase: String = "",
) : SearchQuery<ChannelMetadata>(phrase)