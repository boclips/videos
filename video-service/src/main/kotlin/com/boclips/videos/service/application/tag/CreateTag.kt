package com.boclips.videos.service.application.tag

import com.boclips.videos.service.application.exceptions.NonNullableFieldCreateRequestException.Companion.getOrThrow
import com.boclips.videos.service.application.exceptions.TagExistsException
import com.boclips.videos.service.domain.model.tag.TagRepository
import com.boclips.videos.service.presentation.tag.CreateTagRequest
import com.boclips.videos.service.presentation.tag.TagResource

class CreateTag(
    private val tagRepository: TagRepository
) {
    operator fun invoke(request: CreateTagRequest): TagResource {
        val tagName = getOrThrow(request.name, "name")

        if (tagRepository.findByName(tagName) != null) {
            throw TagExistsException(tagName)
        }
        return tagRepository.create(name = tagName)
            .let { TagResource(id = it.id.value, name = it.name) }
    }
}
