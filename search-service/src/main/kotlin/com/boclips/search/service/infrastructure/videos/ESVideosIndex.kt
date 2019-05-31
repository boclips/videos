package com.boclips.search.service.infrastructure.videos

import com.boclips.search.service.infrastructure.ESIndex

object ESVideosIndex : ESIndex{
    private const val ES_VIDEOS_INDEX_PREFIX = "videos_"
    private const val ES_VIDEOS_INDEX_ALIAS = "current_videos"
    private const val ES_VIDEOS_INDEX_WILDCARD = "$ES_VIDEOS_INDEX_PREFIX*"

    override fun getIndexAlias() = ES_VIDEOS_INDEX_ALIAS
    override fun getIndexWildcard() = ES_VIDEOS_INDEX_WILDCARD

    fun generateIndexName() =
        ESIndex.generateIndexName(ES_VIDEOS_INDEX_PREFIX)
}