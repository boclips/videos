package com.boclips.videos.service.domain.model.video.request

import com.boclips.videos.service.domain.model.AgeRange
import java.time.Duration

data class VideoFacets(
    var ageRanges: List<AgeRange> = listOf(
        AgeRange.bounded(3, 5),
        AgeRange.bounded(5, 9),
        AgeRange.bounded(9, 11),
        AgeRange.bounded(11, 14),
        AgeRange.bounded(14, 16),
        AgeRange.bounded(16, 99)
    ),
    var durations: List<Pair<Duration, Duration>> = listOf(
        Pair(Duration.ZERO, Duration.ofMinutes(2)),
        Pair(Duration.ofMinutes(2), Duration.ofMinutes(5)),
        Pair(Duration.ofMinutes(5), Duration.ofMinutes(10)),
        Pair(Duration.ofMinutes(10), Duration.ofMinutes(20)),
        Pair(Duration.ofMinutes(20), Duration.ofHours(24))
    )
)