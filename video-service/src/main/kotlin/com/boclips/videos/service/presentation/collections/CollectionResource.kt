package com.boclips.videos.service.presentation.collections

import com.boclips.videos.service.presentation.video.VideoResource
import org.springframework.hateoas.Resource
import org.springframework.hateoas.core.Relation
import java.time.Instant

@Relation(collectionRelation = "collections")
data class CollectionResource(
    val id: String,
    val owner: String,
    val title: String,
    val videos: List<Resource<VideoResource>>,
    val updatedAt: Instant,
    val isPublic: Boolean
)