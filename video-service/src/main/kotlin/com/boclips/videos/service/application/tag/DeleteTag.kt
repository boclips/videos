package com.boclips.videos.service.application.tag

import com.boclips.videos.service.domain.model.collection.CollectionRepository
import com.boclips.videos.service.domain.model.tag.TagId
import com.boclips.videos.service.domain.model.tag.TagRepository
import com.boclips.videos.service.domain.service.collection.CollectionSearchService
import com.boclips.videos.service.domain.service.collection.CollectionsUpdateCommand

class DeleteTag(
    private val tagRepository: TagRepository
) {
    operator fun invoke(tagId: TagId) {
        tagRepository.delete(tagId)
    }
}
