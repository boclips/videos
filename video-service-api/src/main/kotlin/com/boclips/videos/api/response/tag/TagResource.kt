package com.boclips.videos.api.response.tag

import org.springframework.hateoas.core.Relation

@Relation(collectionRelation = "tags")
data class TagResource(
    val id: String,
    val label: String? = null,
    val userId: String? = null
)