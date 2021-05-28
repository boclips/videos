package com.boclips.search.service.domain.channels.model

import com.boclips.search.service.domain.common.model.SearchQuery
import com.boclips.search.service.domain.common.model.Sort

class ChannelQuery(
    override val phrase: String = "",
    val accessRuleQuery: ChannelAccessRuleQuery? = null,
    override val sort: List<Sort<ChannelMetadata>> = emptyList(),
    val ingestTypes: List<IngestType> = emptyList(),
) : SearchQuery<ChannelMetadata>(phrase = phrase, sort = sort)

data class ChannelAccessRuleQuery(
    val excludedContentPartnerIds: Set<String> = emptySet(),
    val includedChannelIds: Set<String> = emptySet(),
    val includedTypes: Set<ContentType> = emptySet(),
    val excludedTypes: Set<ContentType> = emptySet(),
    val isEligibleForStream: Boolean? = null,
)
