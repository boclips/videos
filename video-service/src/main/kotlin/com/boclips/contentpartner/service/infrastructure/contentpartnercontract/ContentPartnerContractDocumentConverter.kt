package com.boclips.contentpartner.service.infrastructure.contentpartnercontract

import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContract
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContractCosts
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContractDates
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractId
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContractRestrictions
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContractRoyaltySplit
import mu.KLogging
import org.bson.types.ObjectId
import java.net.MalformedURLException
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.util.Currency

class ContentPartnerContractDocumentConverter() {
    fun toDocument(contract: ContentPartnerContract) =
        ContentPartnerContractDocument(
            id = ObjectId(contract.id.value),
            contentPartnerName = contract.contentPartnerName,
            contractDocument = contract.contractDocument.toString(),
            contractDates = ContractDatesDocument(
                start = contract.contractDates?.start?.toString(),
                end = contract.contractDates?.end?.toString()
            ),
            contractIsRolling = contract.contractIsRolling,
            daysBeforeTerminationWarning = contract.daysBeforeTerminationWarning,
            yearsForMaximumLicense = contract.yearsForMaximumLicense,
            daysForSellOffPeriod = contract.daysForSellOffPeriod,
            royaltySplit = ContractRoyaltySplitDocument(
                download = contract.royaltySplit?.download,
                streaming = contract.royaltySplit?.streaming
            ),
            minimumPriceDescription = contract.minimumPriceDescription,
            remittanceCurrency = contract.remittanceCurrency?.currencyCode,
            restrictions = ContractRestrictionsDocument(
                clientFacing = contract.restrictions.clientFacing,
                territory = contract.restrictions.territory,
                licensing = contract.restrictions.licensing,
                editing = contract.restrictions.editing,
                marketing = contract.restrictions.marketing,
                companies = contract.restrictions.companies,
                payout = contract.restrictions.payout,
                other = contract.restrictions.other
            ),
            costs = ContractCostsDocument(
                minimumGuarantee = contract.costs.minimumGuarantee,
                upfrontLicense = contract.costs.upfrontLicense,
                technicalFee = contract.costs.technicalFee,
                recoupable = contract.costs.recoupable
            )
        )

    fun toContract(document: ContentPartnerContractDocument) =
        ContentPartnerContract(
            id = ContentPartnerContractId(
                document.id.toHexString()
            ),
            contentPartnerName = document.contentPartnerName,
            contractDocument = document.contractDocument?.let {
                try {
                    URL(it)
                } catch (e: MalformedURLException) {
                    null
                }
            },
            contractDates = ContractDates(
                start = document.contractDates?.start?.let { parseDate(document.id, it) },
                end = document.contractDates?.end?.let { parseDate(document.id, it) }
            ),
            contractIsRolling = document.contractIsRolling,
            daysBeforeTerminationWarning = document.daysBeforeTerminationWarning,
            yearsForMaximumLicense = document.yearsForMaximumLicense,
            daysForSellOffPeriod = document.daysForSellOffPeriod,
            royaltySplit = ContractRoyaltySplit(
                download = document.royaltySplit?.download,
                streaming = document.royaltySplit?.streaming
            ),
            minimumPriceDescription = document.minimumPriceDescription,
            remittanceCurrency = document.remittanceCurrency?.let {
                try {
                    Currency.getInstance(it)
                } catch (e: IllegalArgumentException) {
                    null
                }
            },
            restrictions = ContractRestrictions(
                clientFacing = document.restrictions?.clientFacing ?: emptyList(),
                territory = document.restrictions?.territory,
                licensing = document.restrictions?.licensing,
                editing = document.restrictions?.editing,
                marketing = document.restrictions?.marketing,
                companies = document.restrictions?.companies,
                payout = document.restrictions?.payout,
                other = document.restrictions?.other
            ),
            costs = ContractCosts(
                minimumGuarantee = document.costs?.minimumGuarantee ?: emptyList(),
                upfrontLicense = document.costs?.upfrontLicense,
                technicalFee = document.costs?.technicalFee,
                recoupable = document.costs?.recoupable
            )
        )

    private fun parseDate(id: ObjectId, s: String): LocalDate? {
        return try {
            LocalDate.parse(s)
        } catch (e: DateTimeParseException) {
            logger.error {
                "Failed to deserialize date in content partner contract with ID=$id."
            }
            null
        }
    }

    companion object : KLogging()
}
