package com.boclips.search.service.domain.channels.model

import com.boclips.search.service.domain.common.model.SearchQuery
import com.boclips.search.service.domain.videos.model.AccessRuleQuery

class SuggestionQuery<T>(
    override val phrase: String = "",
    val accessRuleQuery: AccessRuleQuery? = null
) : SearchQuery<T>(phrase)