package com.boclips.videos.service.presentation.collections

import com.boclips.videos.service.presentation.video.VideoResource

data class CollectionResource(
    val owner: String? = null,
    val title: String? = null,
    val videos: List<VideoResource>? = null
)