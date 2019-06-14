package com.boclips.videos.service.infrastructure.subject

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class SubjectDocument(
    @BsonId val id: ObjectId,
    val name: String
)
