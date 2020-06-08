package com.boclips.contentpartner.service.domain.model.contract

import java.util.Currency

sealed class ContractUpdateCommand(val contractId: ContractId) {
    class ReplaceContentPartnerName(
        contractId: ContractId,
        val contentPartnerName: String
    ) : ContractUpdateCommand(contractId)

    class ReplaceContractDocument(
        contractId: ContractId,
        val contractDocument: String?
    ) : ContractUpdateCommand(contractId)

    class ReplaceContractIsRolling(contractId: ContractId, val contractIsRolling: Boolean) :
        ContractUpdateCommand(contractId)

    class ReplaceContractDates(contractId: ContractId, val contractDates: ContractDates) :
        ContractUpdateCommand(contractId)

    class ReplaceDaysBeforeTerminationWarning(
        contractId: ContractId,
        val daysBeforeTerminationWarning: Int
    ) : ContractUpdateCommand(contractId)

    class ReplaceYearsForMaximumLicense(
        contractId: ContractId,
        val yearsForMaximumLicense: Int
    ) : ContractUpdateCommand(contractId)

    class ReplaceDaysForSellOffPeriod(
        contractId: ContractId,
        val daysForSellOffPeriod: Int
    ) : ContractUpdateCommand(contractId)

    class ReplaceRoyaltySplit(
        contractId: ContractId,
        val royaltySplit: ContractRoyaltySplit
    ) : ContractUpdateCommand(contractId)

    class ReplaceMinimumPriceDescription(
        contractId: ContractId,
        val minimumPriceDescription: String
    ) : ContractUpdateCommand(contractId)

    class ReplaceRemittanceCurrency(
        contractId: ContractId,
        val remittanceCurrency: Currency
    ) : ContractUpdateCommand(contractId)

    class ReplaceRestrictions(
        contractId: ContractId,
        val restrictions: ContractRestrictions
    ) : ContractUpdateCommand(contractId)

    class ReplaceCost(contractId: ContractId, val costs: ContractCosts) :
        ContractUpdateCommand(contractId)
}
