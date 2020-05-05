package com.boclips.videos.service.application.tag

import com.boclips.videos.service.domain.model.tag.TagId
import com.boclips.videos.service.domain.service.TagRepository

class DeleteTag(
    private val tagRepository: TagRepository
) {
    operator fun invoke(tagId: TagId) {
        tagRepository.delete(tagId)
    }
}
