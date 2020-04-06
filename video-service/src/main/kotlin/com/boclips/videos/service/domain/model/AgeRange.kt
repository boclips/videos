package com.boclips.videos.service.domain.model

sealed class AgeRange {
    companion object {
        fun of(min: Int?, max: Int?): AgeRange {
            return if (min == null && max == null) {
                UnknownAgeRange
            } else if (min == null) {
                CappedAgeRange(max!!)
            } else if (max == null) {
                OpenEndedAgeRange(min)
            } else {
                FixedAgeRange(min, max)
            }
        }
    }

    fun min(): Int? {
        return when (this) {
            is FixedAgeRange -> this.min
            is OpenEndedAgeRange -> this.min
            else -> null
        }
    }

    fun max(): Int? {
        return when (this) {
            is FixedAgeRange -> this.max
            is CappedAgeRange -> this.max
            else -> null
        }
    }
}

data class FixedAgeRange(val min: Int, val max: Int) : AgeRange()
data class OpenEndedAgeRange(val min: Int) : AgeRange()
data class CappedAgeRange(val max: Int) : AgeRange()
object UnknownAgeRange : AgeRange()
