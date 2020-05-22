package com.boclips.videos.service.infrastructure.collection

import com.boclips.videos.service.infrastructure.attachment.AttachmentDocument
import com.boclips.videos.service.infrastructure.subject.SubjectDocument
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.time.Instant

data class CollectionDocument(
    @BsonId val id: ObjectId,
    val ageRangeMax: Int? = null,
    val ageRangeMin: Int? = null,
    val attachments: Set<AttachmentDocument>? = emptySet(),
    val bookmarks: Set<String> = emptySet(),
    val createdAt: Instant?,
    val createdByBoclips: Boolean?,
    val description: String? = null,
    val discoverable: Boolean = false,
    val owner: String,
    val promoted: Boolean? = false,
    val subCollectionIds: Set<String>? = emptySet(),
    val subjects: Set<SubjectDocument> = emptySet(),
    val title: String,
    val updatedAt: Instant,
    val videos: List<String>,
    val default: Boolean = false
)
