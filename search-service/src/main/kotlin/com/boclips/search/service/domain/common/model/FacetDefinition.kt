package com.boclips.search.service.domain.common.model

import com.boclips.search.service.domain.videos.model.AgeRange

sealed class FacetDefinition {
    class Video(val ageRangeBuckets: List<AgeRange>?) : FacetDefinition()
    object Collection : FacetDefinition()
}
