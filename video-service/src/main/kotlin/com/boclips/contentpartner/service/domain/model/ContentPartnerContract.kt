package com.boclips.contentpartner.service.domain.model

import java.net.URL
import java.util.Currency

data class ContentPartnerContract(
    val id: ContentPartnerContractId,
    val contentPartnerName: String,
    val contractDocument: URL?,
    val contractDates: ContentPartnerContractDates?,
    val daysBeforeTerminationWarning: Int?,
    val yearsForMaximumLicense: Int?,
    val daysForSellOffPeriod: Int?,
    val royaltySplit: ContentPartnerContractRoyaltySplit?,
    val minimumPriceDescription: String?,
    val remittanceCurrency: Currency?,
    val restrictions: ContentPartnerContractRestrictions,
    val costs: ContentPartnerContractCosts
)