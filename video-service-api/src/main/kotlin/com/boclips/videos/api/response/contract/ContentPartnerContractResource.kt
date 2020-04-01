package com.boclips.videos.api.response.contract

import com.boclips.videos.api.response.HateoasLink

data class ContentPartnerContractResource(
    val id: String,
    val contentPartnerName: String,
    val contractDocument: String?,
    val contractDates: ContentPartnerContractDatesResource,
    val contractIsRolling: Boolean?,
    val daysBeforeTerminationWarning: Int?,
    val yearsForMaximumLicense: Int?,
    val daysForSellOffPeriod: Int?,
    val royaltySplit: ContentPartnerContractRoyaltySplitResource,
    val minimumPriceDescription: String?,
    val remittanceCurrency: String?,
    val restrictions: ContentPartnerContractRestrictionsResource,
    val costs: ContentPartnerContractCostsResource,
    val _links: Map<String, HateoasLink>
)
