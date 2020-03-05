package com.boclips.search.service.domain.videos.model

data class AgeRange(val min: Int? = null, val max: Int? = null) {
    init {
        if (min == null && max == null) throw RuntimeException()
    }

    fun toRange(): List<Int> {
        val isUnbounded = listOfNotNull(min, max).isEmpty()
        if (isUnbounded) return emptyList()

        return ((min ?: 3)..(max ?: 99)).toList()
    }
}
