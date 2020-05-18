package com.boclips.videos.service.infrastructure.collection

import com.boclips.videos.service.infrastructure.attachment.AttachmentDocument
import com.boclips.videos.service.infrastructure.subject.SubjectDocument
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.time.Instant

data class CollectionDocument(
    @BsonId val id: ObjectId,
    val owner: String,
    val title: String,
    val videos: List<String>,
    val createdAt: Instant?,
    val updatedAt: Instant,
    val discoverable: Boolean = false,
    val createdByBoclips: Boolean?,
    val promoted: Boolean? = false,
    val bookmarks: Set<String> = emptySet(),
    val subjects: Set<SubjectDocument> = emptySet(),
    val ageRangeMin: Int? = null,
    val ageRangeMax: Int? = null,
    val description: String? = null,
    val attachments: Set<AttachmentDocument>? = emptySet()
)
