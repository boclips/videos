package com.boclips.videos.api.response.video

data class VideoFacetsResource(
    val subjects: Map<String, VideoFacetResource>,
    val ageRanges: Map<String, VideoFacetResource>,
    val durations: Map<String, VideoFacetResource>
)

data class VideoFacetResource(val hits: Long)