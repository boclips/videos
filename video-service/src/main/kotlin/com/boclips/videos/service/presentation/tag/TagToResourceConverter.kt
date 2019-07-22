package com.boclips.videos.service.presentation.tag

import com.boclips.videos.service.domain.model.tag.Tag
import com.boclips.videos.service.domain.model.tag.TagId
import org.springframework.hateoas.Resource

class TagToResourceConverter {
    fun wrapTagIdsInResource(tags: Set<TagId>): Set<Resource<TagResource>> =
        tags.map { Resource(TagResource(it.value)) }.toSet()

    fun wrapTagsInResource(tags: List<Tag>): List<Resource<TagResource>> =
        tags.map { Resource(TagResource.from(it)) }
}
