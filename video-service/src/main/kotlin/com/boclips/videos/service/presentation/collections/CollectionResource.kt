package com.boclips.videos.service.presentation.collections

import com.boclips.videos.service.presentation.video.VideoResource
import org.springframework.hateoas.core.Relation

@Relation(collectionRelation = "collections")
data class CollectionResource(
    val id: String,
    val owner: String,
    val title: String,
    val videos: List<VideoResource>
)