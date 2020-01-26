package com.boclips.videos.service.application.tag

import com.boclips.videos.service.domain.model.tag.Tag
import com.boclips.videos.service.domain.model.tag.TagRepository
import com.boclips.web.exceptions.ResourceNotFoundApiException

class GetTag(private val tagRepository: TagRepository) {
    operator fun invoke(tagId: String): Tag {
        return tagRepository.findByIds(listOf(tagId)).firstOrNull()
            ?: throw ResourceNotFoundApiException("Not found", "Tag with id $tagId cannot be found")
    }
}
