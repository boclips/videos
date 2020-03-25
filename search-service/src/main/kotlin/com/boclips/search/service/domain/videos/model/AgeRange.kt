package com.boclips.search.service.domain.videos.model

data class AgeRange(val min: Int? = null, val max: Int? = null) {
    companion object {
        private const val MIN = 3
        private const val MAX = 99
    }

    fun toRange(): List<Int> {
        val isUnbounded = listOfNotNull(min, max).isEmpty()
        if (isUnbounded) return emptyList()

        return ((min ?: MIN)..(max ?: MAX)).toList()
    }

    fun min(): Int {
        return (min ?: MIN)
    }

    fun max(): Int {
        return (max ?: MAX)
    }

    override fun toString(): String {
        return "${(min ?: MIN)}-${(max ?: MAX)}"
    }
}
