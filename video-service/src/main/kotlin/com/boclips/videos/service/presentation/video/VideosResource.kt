package com.boclips.videos.service.presentation.video

import org.springframework.hateoas.Resource

data class VideosResource(
    val videos: List<Resource<VideoResource>>,
    val totalVideos: Long,
    val pageNumber: Int,
    val pageSize: Int
)