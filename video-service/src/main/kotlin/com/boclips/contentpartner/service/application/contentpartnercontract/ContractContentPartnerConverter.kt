package com.boclips.contentpartner.service.application.contentpartnercontract

import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractId
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractUpdateCommand
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractUpdateCommand.ReplaceContentPartnerName
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractUpdateCommand.ReplaceContractDates
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractUpdateCommand.ReplaceContractDocument
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractUpdateCommand.ReplaceContractIsRolling
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractUpdateCommand.ReplaceCost
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractUpdateCommand.ReplaceDaysBeforeTerminationWarning
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractUpdateCommand.ReplaceDaysForSellOffPeriod
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractUpdateCommand.ReplaceMinimumPriceDescription
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractUpdateCommand.ReplaceRemittanceCurrency
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractUpdateCommand.ReplaceRestrictions
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractUpdateCommand.ReplaceRoyaltySplit
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractUpdateCommand.ReplaceYearsForMaximumLicense
import com.boclips.contentpartner.service.presentation.converters.contracts.ContractCostsConverter
import com.boclips.contentpartner.service.presentation.converters.contracts.ContractDatesToResourceConverter
import com.boclips.contentpartner.service.presentation.converters.contracts.ContractRemittanceCurrencyConverter
import com.boclips.contentpartner.service.presentation.converters.contracts.ContractRestrictionsConverter
import com.boclips.contentpartner.service.presentation.converters.contracts.ContractRoyaltySplitConverter
import com.boclips.videos.api.common.ExplicitlyNull
import com.boclips.videos.api.common.Specified
import com.boclips.videos.api.request.contract.ContentPartnerContractRequest

class ContractContentPartnerConverter(
    private val contractDatesConverter: ContractDatesToResourceConverter,
    private val royaltySplitConverter: ContractRoyaltySplitConverter,
    private val remittanceCurrencyConverter: ContractRemittanceCurrencyConverter,
    private val restrictionsConverter: ContractRestrictionsConverter,
    private val costsConverter: ContractCostsConverter
) {
    fun convert(
        id: ContentPartnerContractId,
        updateContract: ContentPartnerContractRequest
    ): List<ContentPartnerContractUpdateCommand> =
        ContractContentPartnerUpdateCreator(
            id,
            updateContract,
            contractDatesConverter,
            royaltySplitConverter,
            remittanceCurrencyConverter,
            restrictionsConverter,
            costsConverter
        ).let { commandCreator ->
            listOfNotNull(
                commandCreator.updateContractContentPartnerName(),
                commandCreator.updateContractIsRolling(),
                commandCreator.updateContractDates(),
                commandCreator.updateDaysBeforeTerminationWarning(),
                commandCreator.updateYearsForMaximumLicense(),
                commandCreator.updateDaysForSellOffPeriod(),
                commandCreator.updateRoyaltySplit(),
                commandCreator.updateMinimumPriceDescription(),
                commandCreator.updateRemittanceCurrency(),
                commandCreator.updateRestrictions(),
                commandCreator.updateCost(),
                commandCreator.updateContractDocument()
            )
        }
}

class ContractContentPartnerUpdateCreator(
    val id: ContentPartnerContractId,
    private val updateContract: ContentPartnerContractRequest,
    private val contractDatesConverter: ContractDatesToResourceConverter,
    private val royaltySplitConverter: ContractRoyaltySplitConverter,
    private val remittanceCurrencyConverter: ContractRemittanceCurrencyConverter,
    private val restrictionsConverter: ContractRestrictionsConverter,
    private val costsConverter: ContractCostsConverter
) {
    fun updateContractContentPartnerName(): ContentPartnerContractUpdateCommand? {
        return ReplaceContentPartnerName(
            contractContentPartnerId = id,
            contentPartnerName = updateContract.contentPartnerName
        )
    }

    fun updateContractDocument(): ContentPartnerContractUpdateCommand? {
        return updateContract.contractDocument?.let {
            ReplaceContractDocument(
                contractContentPartnerId = id,
                contractDocument = when (it) {
                    is Specified -> it.value
                    is ExplicitlyNull -> null
                }
            )
        }
    }

    fun updateContractIsRolling(): ContentPartnerContractUpdateCommand? {
        return updateContract.contractIsRolling?.let {
            ReplaceContractIsRolling(
                contractContentPartnerId = id,
                contractIsRolling = it
            )
        }
    }

    fun updateContractDates(): ContentPartnerContractUpdateCommand? {
        return updateContract.contractDates?.let {
            contractDatesConverter.fromResource(it)?.let { dates ->
                ReplaceContractDates(
                    contractContentPartnerId = id,
                    contractDates = dates
                )
            }
        }
    }

    fun updateDaysBeforeTerminationWarning(): ContentPartnerContractUpdateCommand? {
        return updateContract.daysBeforeTerminationWarning?.let {
            ReplaceDaysBeforeTerminationWarning(
                contractContentPartnerId = id,
                daysBeforeTerminationWarning = it
            )
        }
    }

    fun updateYearsForMaximumLicense(): ContentPartnerContractUpdateCommand? {
        return updateContract.yearsForMaximumLicense?.let {
            ReplaceYearsForMaximumLicense(
                contractContentPartnerId = id,
                yearsForMaximumLicense = it
            )
        }
    }

    fun updateDaysForSellOffPeriod(): ContentPartnerContractUpdateCommand? {
        return updateContract.daysForSellOffPeriod?.let {
            ReplaceDaysForSellOffPeriod(
                contractContentPartnerId = id,
                daysForSellOffPeriod = it
            )
        }
    }

    fun updateRoyaltySplit(): ContentPartnerContractUpdateCommand? {
        return updateContract.royaltySplit?.let {
            ReplaceRoyaltySplit(
                contractContentPartnerId = id,
                royaltySplit = royaltySplitConverter.fromResource(it)
            )
        }
    }

    fun updateMinimumPriceDescription(): ContentPartnerContractUpdateCommand? {
        return updateContract.minimumPriceDescription?.let {
            ReplaceMinimumPriceDescription(
                contractContentPartnerId = id,
                minimumPriceDescription = it
            )
        }
    }

    fun updateRemittanceCurrency(): ContentPartnerContractUpdateCommand? {
        return updateContract.remittanceCurrency?.let {
            ReplaceRemittanceCurrency(
                contractContentPartnerId = id,
                remittanceCurrency = remittanceCurrencyConverter.fromResource(it)
            )
        }
    }

    fun updateRestrictions(): ContentPartnerContractUpdateCommand? {
        return updateContract.restrictions?.let {
            ReplaceRestrictions(
                contractContentPartnerId = id,
                restrictions = restrictionsConverter.fromResource(it)
            )
        }
    }

    fun updateCost(): ContentPartnerContractUpdateCommand? {
        return updateContract.costs?.let {
            ReplaceCost(
                contractContentPartnerId = id,
                costs = costsConverter.fromResource(it)
            )
        }
    }
}
