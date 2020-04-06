package com.boclips.videos.service.domain.model.video.request

import com.boclips.videos.service.domain.model.AgeRange
import java.time.Duration

data class VideoFacets(
    var ageRanges: List<AgeRange> = listOf(
        AgeRange.of(min = 3, max = 5, curatedManually = false),
        AgeRange.of(min = 5, max = 9, curatedManually = false),
        AgeRange.of(min = 9, max = 11, curatedManually = false),
        AgeRange.of(min = 11, max = 14, curatedManually = false),
        AgeRange.of(min = 14, max = 16, curatedManually = false),
        AgeRange.of(min = 16, max = 99, curatedManually = false)
    ),
    var durations: List<Pair<Duration, Duration>> = listOf(
        Pair(Duration.ZERO, Duration.ofMinutes(2)),
        Pair(Duration.ofMinutes(2), Duration.ofMinutes(5)),
        Pair(Duration.ofMinutes(5), Duration.ofMinutes(10)),
        Pair(Duration.ofMinutes(10), Duration.ofMinutes(20)),
        Pair(Duration.ofMinutes(20), Duration.ofHours(24))
    )
)