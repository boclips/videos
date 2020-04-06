package com.boclips.videos.service.domain.model

sealed class AgeRange {
    companion object {
        fun of(min: Int?, max: Int?): AgeRange {
            return if (min == null && max == null) {
                UnknownAgeRange
            } else if (min == null) {
                UpperBoundedAgeRange(max!!)
            } else if (max == null) {
                LowerBoundedAgeRange(min)
            } else {
                SpecificAgeRange(min, max)
            }
        }
    }

    fun min(): Int? {
        return when (this) {
            is SpecificAgeRange -> this.min
            is LowerBoundedAgeRange -> this.min
            else -> null
        }
    }

    fun max(): Int? {
        return when (this) {
            is SpecificAgeRange -> this.max
            is UpperBoundedAgeRange -> this.max
            else -> null
        }
    }
}

data class SpecificAgeRange(val min: Int, val max: Int) : AgeRange()
data class LowerBoundedAgeRange(val min: Int) : AgeRange()
data class UpperBoundedAgeRange(val max: Int) : AgeRange()
object UnknownAgeRange : AgeRange()
