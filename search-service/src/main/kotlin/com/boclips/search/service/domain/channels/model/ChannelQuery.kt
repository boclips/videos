package com.boclips.search.service.domain.channels.model

import com.boclips.search.service.domain.common.model.SearchQuery
import com.boclips.search.service.domain.common.model.Sort

class ChannelQuery(
    // phrase is a fuzzy match
    override val phrase: String = "",
    override val sort: List<Sort<ChannelMetadata>> = emptyList(),
    // name returns exact matches only
    val name: String? = null,
    val accessRuleQuery: ChannelAccessRuleQuery = ChannelAccessRuleQuery(),
    val ingestTypes: List<IngestType> = emptyList(),
    val taxonomy: Taxonomy? = null
) : SearchQuery<ChannelMetadata>(phrase = phrase, sort = sort)

data class ChannelAccessRuleQuery(
    val excludedContentPartnerIds: Set<String> = emptySet(),
    val includedChannelIds: Set<String> = emptySet(),
    val includedTypes: Set<ContentType> = emptySet(),
    val excludedTypes: Set<ContentType> = emptySet(),
    val includedPrivateChannelIds: Set<String> = emptySet(),
    val isEligibleForStream: Boolean? = null,
)
