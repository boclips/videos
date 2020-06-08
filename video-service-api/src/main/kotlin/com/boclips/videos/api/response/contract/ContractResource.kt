package com.boclips.videos.api.response.contract

import com.boclips.videos.api.response.HateoasLink

data class ContractResource(
    val id: String,
    val contentPartnerName: String,
    val contractDocument: String?,
    val contractDates: ContractDatesResource,
    val contractIsRolling: Boolean?,
    val daysBeforeTerminationWarning: Int?,
    val yearsForMaximumLicense: Int?,
    val daysForSellOffPeriod: Int?,
    val royaltySplit: ContractRoyaltySplitResource,
    val minimumPriceDescription: String?,
    val remittanceCurrency: String?,
    val restrictions: ContractRestrictionsResource,
    val costs: ContractCostsResource,
    val _links: Map<String, HateoasLink>
)
