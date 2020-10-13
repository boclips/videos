package com.boclips.search.service.domain.channels.model

import com.boclips.search.service.domain.common.model.SearchQuery
import com.boclips.search.service.domain.videos.model.AccessRuleQuery

class ChannelQuery(
    override val phrase: String = "",
    val accessRuleQuery: AccessRuleQuery
) : SearchQuery<ChannelMetadata>(phrase)