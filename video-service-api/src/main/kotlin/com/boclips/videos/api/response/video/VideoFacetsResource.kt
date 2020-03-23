package com.boclips.videos.api.response.video

data class VideoFacetsResource(val subjects: List<VideoFacet>)

data class VideoFacet(val id: String, val hits: Long)