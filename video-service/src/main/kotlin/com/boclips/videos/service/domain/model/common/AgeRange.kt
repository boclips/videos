package com.boclips.videos.service.domain.model.common

sealed class AgeRange {
    companion object {
        fun bounded(min: Int?, max: Int?) =
            BoundedAgeRange(min, max)

        fun unbounded() =
            UnboundedAgeRange
    }

    fun min() = when (this) {
        is BoundedAgeRange -> this.min
        UnboundedAgeRange -> null
    }

    fun max() = when (this) {
        is BoundedAgeRange -> this.max
        UnboundedAgeRange -> null
    }
}

data class BoundedAgeRange(val min: Int?, val max: Int?) : AgeRange()
object UnboundedAgeRange : AgeRange()
