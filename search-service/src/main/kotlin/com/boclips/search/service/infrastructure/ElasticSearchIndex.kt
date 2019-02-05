package com.boclips.search.service.infrastructure

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class ElasticSearchIndex {
    companion object {
        const val ES_INDEX_ALIAS = "current_videos"
        private const val ES_INDEX_PREFIX = "videos_"
        const val ES_INDEX_WILDCARD = "$ES_INDEX_PREFIX*"

        fun generateIndexName(): String {
            val timestamp = ZonedDateTime.now(ZoneId.of("UTC")).format(DateTimeFormatter.ofPattern("yyyyMMddHmsn"))
            return "$ES_INDEX_PREFIX$timestamp"
        }
    }
}