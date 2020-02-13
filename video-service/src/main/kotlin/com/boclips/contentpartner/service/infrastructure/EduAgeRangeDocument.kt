package com.boclips.contentpartner.service.infrastructure

import org.bson.codecs.pojo.annotations.BsonId

class EduAgeRangeDocument(
    @BsonId
    val id: String,
    val label: String,
    val min: Int,
    val max: Int?
)
