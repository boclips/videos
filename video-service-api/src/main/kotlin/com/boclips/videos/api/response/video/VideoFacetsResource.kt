package com.boclips.videos.api.response.video

data class VideoFacetsResource(val subjects: List<VideoFacetResource>)

data class VideoFacetResource(val id: String, val hits: Long)