package com.boclips.contentpartner.service.infrastructure

import com.boclips.videos.service.infrastructure.legal.restrictions.LegalRestrictionsDocument
import com.boclips.videos.service.infrastructure.video.DistributionMethodDocument
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
    val legalRestrictions: LegalRestrictionsDocument? = null,
    val distributionMethods: Set<DistributionMethodDocument>? = null
) {
}