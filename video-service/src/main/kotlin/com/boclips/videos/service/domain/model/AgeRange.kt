package com.boclips.videos.service.domain.model

sealed class AgeRange {
    companion object {
        fun bounded(min: Int, max: Int?) =
            BoundedAgeRange(min, max)

        fun unbounded() =
            UnboundedAgeRange
    }
}

data class BoundedAgeRange(val min: Int, val max: Int?) : AgeRange()
object UnboundedAgeRange : AgeRange()
