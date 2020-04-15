package com.boclips.contentpartner.service.domain.model.contentpartnercontract

import java.util.Currency

sealed class ContractContentPartnerUpdateCommand(val contractContentPartnerId: ContentPartnerContractId) {
    class ReplaceContentPartnerName(
        contractContentPartnerId: ContentPartnerContractId,
        val contentPartnerName: String
    ) : ContractContentPartnerUpdateCommand(contractContentPartnerId)

    class ReplaceContractIsRolling(contractContentPartnerId: ContentPartnerContractId, val contractIsRolling: Boolean) :
        ContractContentPartnerUpdateCommand(contractContentPartnerId)

    class ReplaceContractDates(contractContentPartnerId: ContentPartnerContractId, val contractDates: ContractDates) :
        ContractContentPartnerUpdateCommand(contractContentPartnerId)

    class ReplaceDaysBeforeTerminationWarning(
        contractContentPartnerId: ContentPartnerContractId,
        val daysBeforeTerminationWarning: Int
    ) : ContractContentPartnerUpdateCommand(contractContentPartnerId)

    class ReplaceYearsForMaximumLicense(
        contractContentPartnerId: ContentPartnerContractId,
        val yearsForMaximumLicense: Int
    ) : ContractContentPartnerUpdateCommand(contractContentPartnerId)

    class ReplaceDaysForSellOffPeriod(
        contractContentPartnerId: ContentPartnerContractId,
        val daysForSellOffPeriod: Int
    ) : ContractContentPartnerUpdateCommand(contractContentPartnerId)

    class ReplaceRoyaltySplit(
        contractContentPartnerId: ContentPartnerContractId,
        val royaltySplit: ContractRoyaltySplit
    ) : ContractContentPartnerUpdateCommand(contractContentPartnerId)

    class ReplaceMinimumPriceDescription(
        contractContentPartnerId: ContentPartnerContractId,
        val minimumPriceDescription: String
    ) : ContractContentPartnerUpdateCommand(contractContentPartnerId)

    class ReplaceRemittanceCurrency(
        contractContentPartnerId: ContentPartnerContractId,
        val remittanceCurrency: Currency
    ) : ContractContentPartnerUpdateCommand(contractContentPartnerId)

    class ReplaceRestrictions(
        contractContentPartnerId: ContentPartnerContractId,
        val restrictions: ContractRestrictions
    ) : ContractContentPartnerUpdateCommand(contractContentPartnerId)

    class ReplaceCost(contractContentPartnerId: ContentPartnerContractId, val costs: ContractCosts) :
        ContractContentPartnerUpdateCommand(contractContentPartnerId)
}