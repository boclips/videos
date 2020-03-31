package com.boclips.contentpartner.service.infrastructure

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.time.Instant

data class ContentPartnerContractDocument(
    @BsonId val id: ObjectId,
    val contentPartnerName: String,
    val contractDocument: String?,
    val contractDates: ContentPartnerContractDatesDocument?,
    val daysBeforeTerminationWarning: Int?,
    val yearsForMaximumLicense: Int?,
    val daysForSellOffPeriod: Int?,
    val royaltySplit: ContentPartnerContractRoyaltySplitDocument?,
    val minimumPriceDescription: String?,
    val remittanceCurrency: String?,
    val lastModified: Instant? = null,
    val createdAt: Instant? = null,
    val restrictions: ContentPartnerContractRestrictionsDocument?,
    val costs: ContentPartnerContractCostsDocument?
)