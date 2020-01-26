package com.boclips.videos.service.application.tag

import com.boclips.videos.service.domain.model.tag.Tag
import com.boclips.videos.service.domain.model.tag.TagRepository

class GetTags(
    private val tagRepository: TagRepository
) {
    operator fun invoke(): List<Tag> {
        return tagRepository.findAll()
    }
}
