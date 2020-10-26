package com.boclips.videos.service.application.video.search

import com.boclips.videos.service.application.common.QueryConverter
import com.boclips.videos.service.domain.model.video.request.FixedAgeRangeFacet
import com.boclips.videos.service.domain.model.video.request.VideoFacets
import java.time.Duration

class FacetConverter {
    operator fun invoke(
        ageRangesFacets: List<FixedAgeRangeFacet>?,
        durationFacets: List<String>?,
        resourcesFacets: List<String>?,
        includeChannelFacets: Boolean?
    ): VideoFacets {
        return VideoFacets()
            .apply {
                durationFacets.let {
                    val durationRanges = QueryConverter().convertDurations(
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
            .apply {
                resourcesFacets?.let { resourcesFacets ->
                    attachmentTypes = resourcesFacets
                }
            }
            .apply { includeChannelFacets?.let {
                this.includeChannelFacets = it
            } }
    }
}
