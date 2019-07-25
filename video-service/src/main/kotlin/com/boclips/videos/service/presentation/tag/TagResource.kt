package com.boclips.videos.service.presentation.tag

import com.boclips.videos.service.domain.model.tag.Tag
import com.boclips.videos.service.domain.model.tag.UserTag
import org.springframework.hateoas.core.Relation

@Relation(collectionRelation = "tags")
data class TagResource(
    val id: String,
    val label: String? = null,
    val userId: String? = null
) {
    companion object {
        fun from(tag: Tag) = TagResource(
            id = tag.id.value,
            label = tag.label
        )

        fun from(userTag: UserTag) = TagResource(
            id = userTag.tag.id.value,
            label = userTag.tag.label,
            userId = userTag.userId.value
        )
    }
}
