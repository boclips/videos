package com.boclips.videos.service.infrastructure.video.mongo

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class UserTagDocument(
    @BsonId val id: ObjectId,
    val label: String,
    val userId: String
)
