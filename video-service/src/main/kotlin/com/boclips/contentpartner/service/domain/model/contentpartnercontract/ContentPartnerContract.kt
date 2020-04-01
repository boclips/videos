package com.boclips.contentpartner.service.domain.model.contentpartnercontract

import java.net.URL
import java.util.Currency

data class ContentPartnerContract(
    val id: ContentPartnerContractId,
    val contentPartnerName: String,
    val contractDocument: URL?,
    val contractIsRolling: Boolean?,
    val contractDates: ContractDates?,
    val daysBeforeTerminationWarning: Int?,
    val yearsForMaximumLicense: Int?,
    val daysForSellOffPeriod: Int?,
    val royaltySplit: ContractRoyaltySplit?,
    val minimumPriceDescription: String?,
    val remittanceCurrency: Currency?,
    val restrictions: ContractRestrictions,
    val costs: ContractCosts
)
