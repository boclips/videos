package com.boclips.videos.service.infrastructure.collection

import com.boclips.videos.service.infrastructure.subject.SubjectDocument
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.time.Instant

data class CollectionDocument(
    @BsonId val id: ObjectId,
    val owner: String,
    val viewerIds: List<String>? = null,
    val title: String,
    val videos: List<String>,
    val updatedAt: Instant,
    val visibility: CollectionVisibilityDocument?,
    val createdByBoclips: Boolean?,
    val bookmarks: Set<String> = emptySet(),
    val subjects: Set<SubjectDocument>? = emptySet(),
    val ageRangeMin: Int? = null,
    val ageRangeMax: Int? = null
)
