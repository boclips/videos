package com.boclips.videos.api.response.contract

import com.boclips.videos.api.response.HateoasLink

data class ContractResource(
    val id: String,
    val contentPartnerName: String,
    val contractDocument: String? = null,
    val contractDates: ContractDatesResource? = null,
    val contractIsRolling: Boolean? = null,
    val daysBeforeTerminationWarning: Int? = null,
    val yearsForMaximumLicense: Int? = null,
    val daysForSellOffPeriod: Int? = null,
    val royaltySplit: ContractRoyaltySplitResource? = null,
    val minimumPriceDescription: String? = null,
    val remittanceCurrency: String? = null,
    val restrictions: ContractRestrictionsResource? = null,
    val costs: ContractCostsResource? = null,
    val _links: Map<String, HateoasLink>
)
