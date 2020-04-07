package com.boclips.contentpartner.service.infrastructure.contract

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.time.Instant

data class ContentPartnerContractDocument(
    @BsonId val id: ObjectId,
    val contentPartnerName: String,
    val contractDocument: String?,
    val contractIsRolling: Boolean?,
    val contractDates: ContractDatesDocument?,
    val daysBeforeTerminationWarning: Int?,
    val yearsForMaximumLicense: Int?,
    val daysForSellOffPeriod: Int?,
    val royaltySplit: ContractRoyaltySplitDocument?,
    val minimumPriceDescription: String?,
    val remittanceCurrency: String?,
    val lastModified: Instant? = null,
    val createdAt: Instant? = null,
    val restrictions: ContractRestrictionsDocument?,
    val costs: ContractCostsDocument?
)
