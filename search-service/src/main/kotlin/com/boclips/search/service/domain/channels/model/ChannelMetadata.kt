package com.boclips.search.service.domain.channels.model

data class ChannelMetadata(
    val id: String,
    val name: String,
    val eligibleForStream: Boolean,
    val contentTypes: List<ContentType>,
    val ingestType: IngestType?,
    val taxonomy: Taxonomy,
    val isYoutube: Boolean?
)
