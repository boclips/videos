package com.boclips.videos.service.application.video.search

import com.boclips.videos.service.domain.model.AgeRange
import com.boclips.videos.service.domain.model.video.request.VideoFacets
import java.time.Duration

class FacetConverter {
    operator fun invoke(ageRangesFacets: List<AgeRange>?, durationFacets: List<String>?): VideoFacets {
        return VideoFacets()
            .apply {
                durationFacets.let {
                    val durationRanges = SearchQueryConverter().convertDurations(
                        durations = durationFacets,
                        minDurationString = null,
                        maxDurationString = null
                    )

                    durations =
                        durationRanges.map { duration -> Pair(duration.min, duration.max ?: Duration.ofHours(24)) }
                }
            }
            .apply {
                ageRangesFacets?.let { ageRangesFacets ->
                    ageRanges = ageRangesFacets
                }
            }
    }
}