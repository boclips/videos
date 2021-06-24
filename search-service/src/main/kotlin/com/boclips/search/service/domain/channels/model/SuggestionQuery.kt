package com.boclips.search.service.domain.channels.model

import com.boclips.search.service.domain.common.model.SearchQuery
import com.boclips.search.service.domain.common.model.Sort

class SuggestionQuery<T>(
    override val phrase: String = "",
    val accessRuleQuery: SuggestionAccessRuleQuery = SuggestionAccessRuleQuery(),
    override val sort: List<Sort<T>> = emptyList()
) : SearchQuery<T>(phrase = phrase, sort = sort)

data class SuggestionAccessRuleQuery(
    val excludedContentPartnerIds: Set<String> = emptySet(),
    val includedChannelIds: Set<String> = emptySet(),
    val includedTypes: Set<ContentType> = emptySet(),
    val excludedTypes: Set<ContentType> = emptySet(),
    val isEligibleForStream: Boolean? = null,
)
