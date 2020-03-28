package com.boclips.videos.service.domain.model.video.request

import com.boclips.videos.service.domain.model.AgeRange

data class VideoFacets(
    val ageRanges: List<AgeRange> = listOf(
        AgeRange.bounded(3, 5),
        AgeRange.bounded(5, 9),
        AgeRange.bounded(9, 11),
        AgeRange.bounded(11, 14),
        AgeRange.bounded(14, 16),
        AgeRange.bounded(16, 99)
    )
)