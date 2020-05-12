package com.boclips.videos.service.infrastructure.search

import com.boclips.search.service.domain.collections.model.CollectionMetadata
import com.boclips.search.service.domain.collections.model.CollectionVisibility
import com.boclips.videos.service.domain.model.attachment.AttachmentType

object CollectionMetadataConverter {
    fun convert(collection: com.boclips.videos.service.domain.model.collection.Collection): CollectionMetadata {
        return CollectionMetadata(
            id = collection.id.value,
            title = collection.title,
            subjectIds = collection.subjects.map { it.id.value },
            owner = collection.owner.value,
            visibility = if (collection.isPublic) CollectionVisibility.PUBLIC else CollectionVisibility.PRIVATE,
            bookmarkedByUsers = collection.bookmarks.map { it.value }.toSet(),
            hasAttachments = collection.attachments.isNotEmpty(),
            hasLessonPlans = if (collection.attachments.isNotEmpty()) collection.attachments.any { it.type == AttachmentType.LESSON_PLAN } else false,
            promoted = collection.promoted,
            description = collection.description,
            ageRangeMin = collection.ageRange.min(),
            ageRangeMax = collection.ageRange.max(),
            updatedAt = collection.updatedAt.toLocalDate(),
            attachmentTypes = collection.attachments.mapTo(HashSet()) { it.type.label }
        )
    }
}
