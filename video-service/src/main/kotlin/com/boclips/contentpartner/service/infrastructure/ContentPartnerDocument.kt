package com.boclips.contentpartner.service.infrastructure

import com.boclips.videos.service.infrastructure.video.DistributionMethodDocument
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.time.Instant

data class ContentPartnerDocument(
    @BsonId val id: ObjectId,
    val youtubeChannelId: String? = null,
    val name: String,
    val ageRanges: List<EduAgeRangeDocument>? = emptyList(),
    val lastModified: Instant? = null,
    val createdAt: Instant? = null,
    val legalRestrictions: LegalRestrictionsDocument? = null,
    val distributionMethods: Set<DistributionMethodDocument>? = null,
    val remittanceCurrency: String? = null,
    val description: String? = null,
    val contentCategories: List<String>? = emptyList(),
    val language: String? = null,
    val hubspotId: String? = null,
    val awards: String? = null,
    val notes: String? = null,
    val contentTypes: List<String>? = null,
    val marketingInformation: MarketingInformationDocument? = null
) {
}
