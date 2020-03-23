package com.boclips.videos.api.request.contract

import com.boclips.videos.api.response.contract.ContentPartnerContractDatesResource
import com.boclips.videos.api.response.contract.ContentPartnerContractRoyaltySplitResource
import org.springframework.lang.NonNull

data class ContentPartnerContractRequest(
    @field:NonNull
    val contentPartnerName: String,
    val contractDocument: String?,
    val contractDates: ContentPartnerContractDatesResource?,
    val daysBeforeTerminationWarning: Int?,
    val yearsForMaximumLicense: Int?,
    val daysForSellOffPeriod: Int?,
    val royaltySplit: ContentPartnerContractRoyaltySplitResource?,
    val minimumPriceDescription: String?,
    val remittanceCurrency: String?
)