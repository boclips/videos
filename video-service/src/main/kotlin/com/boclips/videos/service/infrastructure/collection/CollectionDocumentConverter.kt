package com.boclips.videos.service.infrastructure.collection

import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.common.AgeRange
import com.boclips.videos.service.domain.model.common.UserId
import com.boclips.videos.service.domain.model.subject.Subject
import com.boclips.videos.service.domain.model.subject.SubjectId
import com.boclips.videos.service.domain.model.video.VideoId

object CollectionDocumentConverter {
    fun toCollection(collectionDocument: CollectionDocument?): Collection? {
        if (collectionDocument == null) return null
        val videoIds = collectionDocument.videos.map { VideoId(value = it) }
        val subjects = collectionDocument.subjects.orEmpty().map {
            Subject(
                id = SubjectId(value = it.id.toHexString()),
                name = it.name
            )
        }.toSet()
        val isPubliclyVisible = collectionDocument.visibility == CollectionVisibilityDocument.PUBLIC

        return Collection(
            id = CollectionId(value = collectionDocument.id.toHexString()),
            title = collectionDocument.title,
            owner = UserId(value = collectionDocument.owner),
            viewerIds = collectionDocument.viewerIds?.map { UserId(it) } ?: emptyList(),
            videos = videoIds,
            updatedAt = collectionDocument.updatedAt,
            isPublic = isPubliclyVisible,
            createdByBoclips = collectionDocument.createdByBoclips ?: false,
            bookmarks = collectionDocument.bookmarks.map { UserId(it) }.toSet(),
            subjects = subjects,
            ageRange = if (collectionDocument.ageRangeMin !== null) AgeRange.bounded(
                min = collectionDocument.ageRangeMin,
                max = collectionDocument.ageRangeMax
            ) else AgeRange.unbounded(),
            description = collectionDocument.description
        )
    }
}
