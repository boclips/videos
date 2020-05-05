package com.boclips.videos.service.application.tag

import com.boclips.videos.api.request.tag.CreateTagRequest
import com.boclips.videos.api.response.tag.TagResource
import com.boclips.videos.service.application.exceptions.TagExistsException
import com.boclips.videos.service.domain.service.TagRepository

open class CreateTag(
    private val tagRepository: TagRepository
) {
    operator fun invoke(request: CreateTagRequest): TagResource {
        val tagLabel = request.label!!

        if (tagRepository.findByLabel(tagLabel) != null) {
            throw TagExistsException(tagLabel)
        }

        return tagRepository.create(label = tagLabel)
            .let { TagResource(id = it.id.value, label = it.label, _links = emptyMap()) }
    }
}
