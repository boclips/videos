package com.boclips.videos.service.infrastructure.contentPartner

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class ContentPartnerDocument(
    @BsonId val id: ObjectId,
    val name: String,
    val ageRangeMin: Int?,
    val ageRangeMax: Int?
)