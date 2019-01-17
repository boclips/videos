package com.boclips.videos.service.domain.model.collection

import com.boclips.videos.service.domain.model.Video

data class Collection(
        val id: CollectionId,
        val owner: String,
        val title: String,
        val videos: List<Video>
)