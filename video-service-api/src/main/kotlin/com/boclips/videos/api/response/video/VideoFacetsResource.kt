package com.boclips.videos.api.response.video

data class VideoFacetsResource(
    val subjects: Map<String, VideoFacetResource>,
    val ageRanges: Map<String, VideoFacetResource>,
    val durations: Map<String, VideoFacetResource>,
    val resourceTypes: Map<String, VideoFacetResource>,
    val videoTypes: Map<String, VideoFacetResource>,
    val channels: Map<String, VideoFacetResource>
)

data class VideoFacetResource(val hits: Long, val id: String? = null, val name: String? = null)
