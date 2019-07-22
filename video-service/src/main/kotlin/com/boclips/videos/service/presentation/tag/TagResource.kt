package com.boclips.videos.service.presentation.tag

import com.boclips.videos.service.domain.model.tag.Tag
import org.springframework.hateoas.core.Relation

@Relation(collectionRelation = "tags")
data class TagResource(
    val id: String,
    val name: String? = null
) {
    companion object {
        fun from(tag: Tag) = TagResource(
            id = tag.id.value,
            name = tag.name
        )
    }
}
