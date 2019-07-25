package com.boclips.videos.service.application.tag

import com.boclips.videos.service.application.exceptions.NonNullableFieldCreateRequestException.Companion.getOrThrow
import com.boclips.videos.service.application.exceptions.TagExistsException
import com.boclips.videos.service.domain.model.tag.TagRepository
import com.boclips.videos.service.presentation.tag.CreateTagRequest
import com.boclips.videos.service.presentation.tag.TagResource


open class CreateTag(
    private val tagRepository: TagRepository
) {
    operator fun invoke(request: CreateTagRequest): TagResource {
        val tagLabel = getOrThrow(request.label, "label")

        if (tagRepository.findByLabel(tagLabel) != null) {
            throw TagExistsException(tagLabel)
        }
        return tagRepository.create(label = tagLabel)
            .let { TagResource(id = it.id.value, label = it.label) }
    }
}
