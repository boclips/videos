package com.boclips.videos.service.presentation.collections

import com.boclips.videos.service.presentation.video.VideoResource
import org.springframework.hateoas.core.Relation

@Relation(collectionRelation = "collections")
data class CollectionResource(
    val id: String? = null,
    val owner: String? = null,
    val title: String? = null,
    val videos: List<VideoResource>? = null
)