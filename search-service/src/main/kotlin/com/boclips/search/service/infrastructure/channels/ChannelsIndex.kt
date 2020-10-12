package com.boclips.search.service.infrastructure.channels

import com.boclips.search.service.infrastructure.Index

object ChannelsIndex : Index {

    private const val ES_CHANNELS_INDEX_PREFIX = "channels_"
    private const val ES_CHANNELS_INDEX_ALIAS = "current_channels"
    private const val ES_CHANNELS_INDEX_WILDCARD = "$ES_CHANNELS_INDEX_PREFIX*"

    override fun getESType() = "channel"
    override fun getIndexAlias() = ES_CHANNELS_INDEX_ALIAS
    override fun getIndexWildcard() = ES_CHANNELS_INDEX_WILDCARD
    override fun generateIndexName() = Index.generateIndexName(ES_CHANNELS_INDEX_PREFIX)
}
