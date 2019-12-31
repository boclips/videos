package com.boclips.videos.service.application.tag

import com.boclips.videos.api.response.tag.TagResource
import com.boclips.videos.service.domain.model.tag.TagRepository

class GetTags(
    private val tagRepository: TagRepository
) {
    operator fun invoke(): List<TagResource> {
        return tagRepository.findAll()
            .map { tag -> TagResource(id = tag.id.value, label = tag.label) }
    }
}
