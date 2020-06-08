package com.boclips.contentpartner.service.infrastructure.channel

import com.boclips.contentpartner.service.infrastructure.agerange.AgeRangeDocument
import com.boclips.contentpartner.service.infrastructure.contract.ContractDocument
import com.boclips.contentpartner.service.infrastructure.legalrestriction.LegalRestrictionsDocument
import com.boclips.videos.service.infrastructure.video.DistributionMethodDocument
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.time.Instant

data class ChannelDocument(
    @BsonId val id: ObjectId,
    val youtubeChannelId: String? = null,
    val name: String,
    val ageRanges: List<AgeRangeDocument>? = emptyList(),
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
    val ingest: IngestDetailsDocument? = null,
    val deliveryFrequency: String? = null,
    val marketingInformation: MarketingInformationDocument? = null,
    val isTranscriptProvided: Boolean? = null,
    val educationalResources: String? = null,
    val curriculumAligned: String? = null,
    val bestForTags: List<String>? = null,
    val subjects: List<String>? = null,
    val contract: ContractDocument?
) {
}
