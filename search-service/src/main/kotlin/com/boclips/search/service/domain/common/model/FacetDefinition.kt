package com.boclips.search.service.domain.common.model

import com.boclips.search.service.domain.videos.model.AgeRange
import com.boclips.search.service.domain.videos.model.DurationRange

sealed class FacetDefinition {
    class Video(
        val ageRangeBuckets: List<AgeRange>?,
        val duration: List<DurationRange>?,
        val resourceTypes: List<String>,
        val includeChannelFacets: Boolean,
        val includePriceFacets: Boolean = false,
        val videoTypes: List<String>
    ) : FacetDefinition()

    object Collection : FacetDefinition()
}
