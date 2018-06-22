package com.boclips.cleanser.domain.model

class MediaFilter(val key: MediaFilterType, val value: String) {
    override fun toString(): String {
        return "MediaFilter: ${this.key.filterKey}=${this.value}"
    }
}

enum class MediaFilterType(val filterKey: String) {
    STATUS_IN("filter[statusIn]"),
    CREATED_AT_GREATER_THAN_OR_EQUAL("filter[createdAtGreaterThanOrEqual]"),
    CREATED_AT_LESS_THAN_OR_EQUAL("filter[createdAtLessThanOrEqual]"),
    STATUS_NOT_EQUAL("filter[statusNotEqual]");
}
