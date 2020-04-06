package com.boclips.videos.service.domain.model

sealed class AgeRange {
    abstract val curatedManually: Boolean

    companion object {
        fun of(min: Int?, max: Int?, curatedManually: Boolean = false): AgeRange {
            return if (min == null && max == null) {
                UnknownAgeRange(curatedManually = curatedManually)
            } else if (min == null) {
                CappedAgeRange(max = max!!, curatedManually = curatedManually)
            } else if (max == null) {
                OpenEndedAgeRange(min = min, curatedManually = curatedManually)
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

data class FixedAgeRange(val min: Int, val max: Int, override val curatedManually: Boolean) : AgeRange()
data class OpenEndedAgeRange(val min: Int, override val curatedManually: Boolean) : AgeRange()
data class CappedAgeRange(val max: Int, override val curatedManually: Boolean) : AgeRange()
data class UnknownAgeRange(override val curatedManually: Boolean = false) : AgeRange()
