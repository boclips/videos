package com.boclips.contentpartner.service.infrastructure.agerange

import org.bson.codecs.pojo.annotations.BsonId

class AgeRangeDocument(
    @BsonId
    val id: String,
    val label: String,
    val min: Int,
    val max: Int?
)
