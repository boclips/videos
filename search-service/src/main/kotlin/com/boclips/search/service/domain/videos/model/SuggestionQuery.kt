package com.boclips.search.service.domain.videos.model

import com.boclips.search.service.domain.common.model.SearchQuery

data class SuggestionQuery(
    override val phrase: String = ""
) : SearchQuery<ChannelMetadata>(phrase)
