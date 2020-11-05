package com.boclips.videos.service.domain.model.video.request

import com.boclips.videos.service.domain.model.attachment.AttachmentType
import com.boclips.videos.service.domain.model.video.ContentType
import java.time.Duration

data class VideoFacets(
    var ageRanges: List<FixedAgeRangeFacet> = listOf(
        FixedAgeRangeFacet(min = 3, max = 5),
        FixedAgeRangeFacet(min = 5, max = 9),
        FixedAgeRangeFacet(min = 9, max = 11),
        FixedAgeRangeFacet(min = 11, max = 14),
        FixedAgeRangeFacet(min = 14, max = 16),
        FixedAgeRangeFacet(min = 16, max = 99)
    ),
    var durations: List<Pair<Duration, Duration>> = listOf(
        Pair(Duration.ZERO, Duration.ofMinutes(2)),
        Pair(Duration.ofMinutes(2), Duration.ofMinutes(5)),
        Pair(Duration.ofMinutes(5), Duration.ofMinutes(10)),
        Pair(Duration.ofMinutes(10), Duration.ofMinutes(20)),
        Pair(Duration.ofMinutes(20), Duration.ofHours(24))
    ),
    var attachmentTypes: List<String> = listOf(AttachmentType.values().toString()),
    var videoTypes: List<String> = listOf(ContentType.values().toString()),
    var includeChannelFacets: Boolean = false
)

class FixedAgeRangeFacet(val min: Int, val max: Int)
