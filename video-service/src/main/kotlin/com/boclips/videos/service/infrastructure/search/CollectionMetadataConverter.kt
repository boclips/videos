package com.boclips.videos.service.infrastructure.search

import com.boclips.search.service.domain.collections.model.CollectionMetadata
import com.boclips.videos.service.domain.model.attachment.AttachmentType
import com.boclips.videos.service.domain.model.collection.Collection

object CollectionMetadataConverter {
    fun convert(collection: Collection): CollectionMetadata {
        return CollectionMetadata(
            id = collection.id.value,
            title = collection.title,
            subjectIds = collection.subjects.map { it.id.value },
            owner = collection.owner.value,
            discoverable = collection.discoverable,
            bookmarkedByUsers = collection.bookmarks.map { it.value }.toSet(),
            hasAttachments = collection.attachments.isNotEmpty(),
            hasLessonPlans = if (collection.attachments.isNotEmpty()) collection.attachments.any { it.type == AttachmentType.LESSON_PLAN } else false,
            promoted = collection.promoted,
            description = collection.description,
            ageRangeMin = collection.ageRange.min(),
            ageRangeMax = collection.ageRange.max(),
            lastModified = collection.updatedAt,
            attachmentTypes = collection.attachments.mapTo(HashSet()) { it.type.label },
            default = collection.default
        )
    }
}
