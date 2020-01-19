package com.boclips.videos.service.presentation.converters

import com.boclips.videos.api.response.tag.TagResource
import com.boclips.videos.service.domain.model.tag.Tag
import com.boclips.videos.service.domain.model.tag.UserTag

class TagConverter {
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
