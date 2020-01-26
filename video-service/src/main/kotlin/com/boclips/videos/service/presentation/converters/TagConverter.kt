package com.boclips.videos.service.presentation.converters

import com.boclips.videos.api.response.tag.TagResource
import com.boclips.videos.api.response.tag.TagsResource
import com.boclips.videos.api.response.tag.TagsWrapperResource
import com.boclips.videos.service.domain.model.tag.Tag
import com.boclips.videos.service.domain.model.tag.UserTag
import com.boclips.videos.service.presentation.hateoas.TagsLinkBuilder

class TagConverter(private val tagsLinkBuilder: TagsLinkBuilder) {
    fun convert(tag: Tag) = TagResource(
        id = tag.id.value,
        label = tag.label,
        _links = listOfNotNull(tagsLinkBuilder.tag("self", tag.id.value))
            .map { it.rel.value() to it }
            .toMap()
    )

    fun convert(tags: List<Tag>): TagsResource {
        val tagsResources = tags.map { convert(it) }

        return TagsResource(
            _embedded = TagsWrapperResource(tags = tagsResources),
            _links = listOfNotNull(tagsLinkBuilder.tags("self")).map { it.rel.value() to it }.toMap()
        )
    }

    fun convert(userTag: UserTag) = TagResource(
        id = userTag.tag.id.value,
        label = userTag.tag.label,
        userId = userTag.userId.value,
        _links = emptyMap()
    )
}
