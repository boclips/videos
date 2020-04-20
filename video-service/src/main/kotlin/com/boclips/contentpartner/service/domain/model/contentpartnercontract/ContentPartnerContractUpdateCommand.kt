package com.boclips.contentpartner.service.domain.model.contentpartnercontract

import com.boclips.videos.api.common.Specifiable
import java.util.Currency

sealed class ContentPartnerContractUpdateCommand(val contractContentPartnerId: ContentPartnerContractId) {
    class ReplaceContentPartnerName(
        contractContentPartnerId: ContentPartnerContractId,
        val contentPartnerName: String
    ) : ContentPartnerContractUpdateCommand(contractContentPartnerId)

    class ReplaceContractDocument(
        contractContentPartnerId: ContentPartnerContractId,
        val contractDocument: String?
    ) : ContentPartnerContractUpdateCommand(contractContentPartnerId)

    class ReplaceContractIsRolling(contractContentPartnerId: ContentPartnerContractId, val contractIsRolling: Boolean) :
        ContentPartnerContractUpdateCommand(contractContentPartnerId)

    class ReplaceContractDates(contractContentPartnerId: ContentPartnerContractId, val contractDates: ContractDates) :
        ContentPartnerContractUpdateCommand(contractContentPartnerId)

    class ReplaceDaysBeforeTerminationWarning(
        contractContentPartnerId: ContentPartnerContractId,
        val daysBeforeTerminationWarning: Int
    ) : ContentPartnerContractUpdateCommand(contractContentPartnerId)

    class ReplaceYearsForMaximumLicense(
        contractContentPartnerId: ContentPartnerContractId,
        val yearsForMaximumLicense: Int
    ) : ContentPartnerContractUpdateCommand(contractContentPartnerId)

    class ReplaceDaysForSellOffPeriod(
        contractContentPartnerId: ContentPartnerContractId,
        val daysForSellOffPeriod: Int
    ) : ContentPartnerContractUpdateCommand(contractContentPartnerId)

    class ReplaceRoyaltySplit(
        contractContentPartnerId: ContentPartnerContractId,
        val royaltySplit: ContractRoyaltySplit
    ) : ContentPartnerContractUpdateCommand(contractContentPartnerId)

    class ReplaceMinimumPriceDescription(
        contractContentPartnerId: ContentPartnerContractId,
        val minimumPriceDescription: String
    ) : ContentPartnerContractUpdateCommand(contractContentPartnerId)

    class ReplaceRemittanceCurrency(
        contractContentPartnerId: ContentPartnerContractId,
        val remittanceCurrency: Currency
    ) : ContentPartnerContractUpdateCommand(contractContentPartnerId)

    class ReplaceRestrictions(
        contractContentPartnerId: ContentPartnerContractId,
        val restrictions: ContractRestrictions
    ) : ContentPartnerContractUpdateCommand(contractContentPartnerId)

    class ReplaceCost(contractContentPartnerId: ContentPartnerContractId, val costs: ContractCosts) :
        ContentPartnerContractUpdateCommand(contractContentPartnerId)
}
