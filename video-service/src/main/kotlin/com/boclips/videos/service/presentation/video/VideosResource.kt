package com.boclips.videos.service.presentation.video

data class VideosResource(
    val videos: List<VideoResource>,
    val totalVideos: Long,
    val pageNumber: Int,
    val pageSize: Int
)