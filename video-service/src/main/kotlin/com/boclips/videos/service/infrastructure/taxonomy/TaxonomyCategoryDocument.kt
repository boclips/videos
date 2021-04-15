package com.boclips.videos.service.infrastructure.taxonomy

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class TaxonomyCategoryDocument (
    @BsonId
    val id: ObjectId,
    val codeValue: String,
    val codeDescription: String,
    val codeParent: String?
)
