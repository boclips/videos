package com.boclips.videos.service.infrastructure.contentPartner

import com.boclips.videos.service.infrastructure.video.mongo.DeliveryMethodDocument
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.time.Instant

data class ContentPartnerDocument(
    @BsonId val id: ObjectId,
    val youtubeChannelId: String? = null,
    val name: String,
    val ageRangeMin: Int?,
    val ageRangeMax: Int?,
    val lastModified: Instant? = null,
    val createdAt: Instant? = null,
    val hiddenFromSearchForDeliveryMethods: Set<DeliveryMethodDocument>? = null
)
