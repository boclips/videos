package com.boclips.videos.service.infrastructure.discipline

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class DisciplineDocument(
    @BsonId val id: ObjectId,
    val name: String,
    val code: String,
    val subjects: List<ObjectId>
)
