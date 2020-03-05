package com.boclips.search.service.infrastructure.common

interface HasAgeRange {
    val ageRangeMin: Int?
    val ageRangeMax: Int?
    companion object {
        const val AGE_RANGE_MIN = "ageRangeMin"
        const val AGE_RANGE_MAX = "ageRangeMax"
        const val AGE_RANGE = "ageRange"
    }
}
