package com.boclips.videos.service.infrastructure.video

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.time.Instant

data class ContentPartnerDocument(
    @BsonId val id: ObjectId,
    val name: String,
    val lastModified: Instant? = null,
    val createdAt: Instant? = null
)
