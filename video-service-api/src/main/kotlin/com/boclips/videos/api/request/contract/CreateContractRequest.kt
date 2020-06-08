package com.boclips.videos.api.request.contract

import com.boclips.videos.api.common.Specifiable
import com.boclips.videos.api.response.contract.ContractDatesResource
import com.boclips.videos.api.response.contract.ContractRoyaltySplitResource
import org.springframework.lang.NonNull

data class CreateContractRequest(
    @field:NonNull
    val contentPartnerName: String,
    val contractDocument: Specifiable<String>? = null,
    val contractDates: ContractDatesResource? = null,
    val contractIsRolling: Boolean? = null,
    val daysBeforeTerminationWarning: Int? = null,
    val yearsForMaximumLicense: Int? = null,
    val daysForSellOffPeriod: Int? = null,
    val royaltySplit: ContractRoyaltySplitResource? = null,
    val minimumPriceDescription: String? = null,
    val remittanceCurrency: String? = null,
    val restrictions: ContractRestrictionsRequest? = null,
    val costs: ContractCostsRequest? = null
)
