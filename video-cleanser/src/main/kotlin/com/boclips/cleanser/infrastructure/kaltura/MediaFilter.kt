package com.boclips.cleanser.infrastructure.kaltura

class MediaFilter(val key: MediaFilterType, val value: String)

enum class MediaFilterType(val filterKey: String) {
    CREATED_AT_GREATER_THAN_OR_EQUAL("filter[createdAtGreaterThanOrEqual]"),
    CREATED_AT_LESS_THAN_OR_EQUAL("filter[createdAtLessThanOrEqual]");

    fun getLabel(): String {
        return filterKey
    }
}
