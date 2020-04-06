package com.boclips.videos.service.domain.model

sealed class AgeRange(open val curatedManually: Boolean) {
    companion object {
        fun of(min: Int?, max: Int?, curatedManually: Boolean): AgeRange {
            return if (min == null && max == null) {
                UnknownAgeRange
            } else if (min == null) {
                CappedAgeRange(max = max!!, curatedManually = curatedManually)
            } else if (max == null) {
                OpenEndedAgeRange(min = min, curatedManually = curatedManually)
            } else if (min > max) {
                throw IllegalAgeRange()
            } else {
                FixedAgeRange(min = min, max = max, curatedManually = curatedManually)
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

data class FixedAgeRange(val min: Int, val max: Int, override val curatedManually: Boolean) : AgeRange(curatedManually)
data class OpenEndedAgeRange(val min: Int, override val curatedManually: Boolean) : AgeRange(curatedManually)
data class CappedAgeRange(val max: Int, override val curatedManually: Boolean) : AgeRange(curatedManually)
object UnknownAgeRange : AgeRange(curatedManually = false)

class IllegalAgeRange : Exception()