package com.boclips.videos.service.infrastructure.collection

import org.bson.codecs.pojo.annotations.BsonId
import java.time.Instant

data class CollectionDocument(
    @BsonId val id: String,
    val owner: String,
    val title: String,
    val videos: List<String>,
    val updatedAt: Instant
)
