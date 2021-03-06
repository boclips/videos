package com.boclips.videos.service.infrastructure.collection

import com.boclips.videos.service.domain.model.AgeRange
import com.boclips.videos.service.domain.model.collection.Collection
import com.boclips.videos.service.domain.model.collection.CollectionId
import com.boclips.videos.service.domain.model.subject.Subject
import com.boclips.videos.service.domain.model.subject.SubjectId
import com.boclips.videos.service.domain.model.user.UserId
import com.boclips.videos.service.domain.model.video.VideoId
import com.boclips.videos.service.infrastructure.attachment.AttachmentDocumentConverter
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime

object CollectionDocumentConverter {
    fun toCollection(collectionDocument: CollectionDocument?): Collection? {
        if (collectionDocument == null) return null

        val videoIds = collectionDocument.videos.map { VideoId(value = it) }
        val subjects = collectionDocument.subjects.map {
            Subject(
                id = SubjectId(value = it.id.toHexString()),
                name = it.name
            )
        }.toSet()

        return Collection(
            id = CollectionId(value = collectionDocument.id.toHexString()),
            title = collectionDocument.title,
            owner = UserId(value = collectionDocument.owner),
            videos = videoIds,
            createdAt = ZonedDateTime.ofInstant(
                collectionDocument.createdAt ?: Instant.ofEpochSecond(collectionDocument.id.timestamp.toLong()),
                ZoneOffset.UTC
            ),
            updatedAt = ZonedDateTime.ofInstant(collectionDocument.updatedAt, ZoneOffset.UTC),
            discoverable = collectionDocument.discoverable,
            promoted = collectionDocument.promoted ?: false,
            createdByBoclips = collectionDocument.createdByBoclips ?: false,
            bookmarks = collectionDocument.bookmarks.map {
                UserId(
                    it
                )
            }.toSet(),
            subjects = subjects,
            ageRange = AgeRange.of(
                min = collectionDocument.ageRangeMin,
                max = collectionDocument.ageRangeMax,
                curatedManually = true
            ),
            description = collectionDocument.description,
            attachments = collectionDocument.attachments?.map {
                AttachmentDocumentConverter.convert(it)
            }?.toSet().orEmpty(),
            subCollections = emptyList(),
            default = collectionDocument.default
        )
    }
}
