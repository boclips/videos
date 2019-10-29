package com.boclips.videos.service.infrastructure.attachment

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class AttachmentDocument(
    @BsonId
    val id: ObjectId,
    val description: String? = "",
    val type: String,
    val linkToResource: String? = ""
)
