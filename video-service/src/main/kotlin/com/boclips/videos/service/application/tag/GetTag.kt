package com.boclips.videos.service.application.tag

import com.boclips.videos.service.domain.model.tag.TagRepository
import com.boclips.videos.service.presentation.tag.TagResource
import com.boclips.web.exceptions.ResourceNotFoundApiException

class GetTag(
    private val tagRepository: TagRepository
) {
    operator fun invoke(tagId: String): TagResource {
        return tagRepository.findByIds(listOf(tagId)).firstOrNull()?.let {
            TagResource(
                id = it.id.value,
                name = it.name
            )
        } ?: throw ResourceNotFoundApiException("Not found", "Tag with id $tagId cannot be found")
    }
}
