package com.boclips.videos.service.domain.model.common

import com.boclips.eventbus.domain.AgeRange as EventAgeRange

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

    fun toEvent(): EventAgeRange {
        return EventAgeRange.builder().min(min()).max(max()).build()
    }
}

data class BoundedAgeRange(val min: Int?, val max: Int?) : AgeRange()
object UnboundedAgeRange : AgeRange()
