package com.boclips.videos.api.request.contract

import com.boclips.videos.api.response.contract.ContentPartnerContractDatesResource
import com.boclips.videos.api.response.contract.ContentPartnerContractRoyaltySplitResource
import org.springframework.lang.NonNull

data class ContentPartnerContractRequest(
    @field:NonNull
    val contentPartnerName: String,
    val contractDocument: String? = null,
    val contractDates: ContentPartnerContractDatesResource? = null,
    val contractIsRolling: Boolean? = null,
    val daysBeforeTerminationWarning: Int? = null,
    val yearsForMaximumLicense: Int? = null,
    val daysForSellOffPeriod: Int? = null,
    val royaltySplit: ContentPartnerContractRoyaltySplitResource? = null,
    val minimumPriceDescription: String? = null,
    val remittanceCurrency: String? = null,
    val restrictions: ContentPartnerContractRestrictionsRequest? = null,
    val costs: ContentPartnerContractCostsRequest? = null
)