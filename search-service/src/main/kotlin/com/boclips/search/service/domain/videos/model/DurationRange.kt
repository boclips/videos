package com.boclips.search.service.domain.videos.model

import java.time.Duration

data class DurationRange(
    val min: Duration,
    val max: Duration? = null
) {
    companion object {
        private const val MAX = 1440L
    }

    fun min(): Long {
        return min.seconds
    }

    fun max(): Long {
        return (max?.seconds ?: MAX)
    }

    override fun toString(): String {
        return "${(min)}-${(max ?: MAX)}"
    }
}
