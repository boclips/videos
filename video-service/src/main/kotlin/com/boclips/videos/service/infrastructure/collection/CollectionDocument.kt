package com.boclips.videos.service.infrastructure.collection

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.time.Instant

data class CollectionDocument(
    @BsonId val id: ObjectId,
    val owner: String,
    val title: String,
    val videos: List<String>,
    val updatedAt: Instant
)
