package com.boclips.contentpartner.service.application.contentpartnercontract

import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractId
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContractContentPartnerUpdateCommand
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContractContentPartnerUpdateCommand.ReplaceContentPartnerName
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContractContentPartnerUpdateCommand.ReplaceContractDates
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContractContentPartnerUpdateCommand.ReplaceContractDocument
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContractContentPartnerUpdateCommand.ReplaceContractIsRolling
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContractContentPartnerUpdateCommand.ReplaceCost
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContractContentPartnerUpdateCommand.ReplaceDaysBeforeTerminationWarning
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContractContentPartnerUpdateCommand.ReplaceDaysForSellOffPeriod
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContractContentPartnerUpdateCommand.ReplaceMinimumPriceDescription
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContractContentPartnerUpdateCommand.ReplaceRemittanceCurrency
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContractContentPartnerUpdateCommand.ReplaceRestrictions
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContractContentPartnerUpdateCommand.ReplaceRoyaltySplit
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContractContentPartnerUpdateCommand.ReplaceYearsForMaximumLicense
import com.boclips.contentpartner.service.presentation.converters.contracts.ContractCostsConverter
import com.boclips.contentpartner.service.presentation.converters.contracts.ContractDatesToResourceConverter
import com.boclips.contentpartner.service.presentation.converters.contracts.ContractRemittanceCurrencyConverter
import com.boclips.contentpartner.service.presentation.converters.contracts.ContractRestrictionsConverter
import com.boclips.contentpartner.service.presentation.converters.contracts.ContractRoyaltySplitConverter
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
    ): List<ContractContentPartnerUpdateCommand> =
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
    fun updateContractContentPartnerName(): ContractContentPartnerUpdateCommand? {
        return ReplaceContentPartnerName(
            contractContentPartnerId = id,
            contentPartnerName = updateContract.contentPartnerName
        )
    }

    fun updateContractDocument(): ContractContentPartnerUpdateCommand? {
        return updateContract.contractDocument?.let {
            ReplaceContractDocument(
                contractContentPartnerId = id,
                contractDocument = it
            )
        }
    }

    fun updateContractIsRolling(): ContractContentPartnerUpdateCommand? {
        return updateContract.contractIsRolling?.let {
            ReplaceContractIsRolling(
                contractContentPartnerId = id,
                contractIsRolling = it
            )
        }
    }

    fun updateContractDates(): ContractContentPartnerUpdateCommand? {
        return updateContract.contractDates?.let {
            contractDatesConverter.fromResource(it)?.let { dates ->
                ReplaceContractDates(
                    contractContentPartnerId = id,
                    contractDates = dates
                )
            }
        }
    }

    fun updateDaysBeforeTerminationWarning(): ContractContentPartnerUpdateCommand? {
        return updateContract.daysBeforeTerminationWarning?.let {
            ReplaceDaysBeforeTerminationWarning(
                contractContentPartnerId = id,
                daysBeforeTerminationWarning = it
            )
        }
    }

    fun updateYearsForMaximumLicense(): ContractContentPartnerUpdateCommand? {
        return updateContract.yearsForMaximumLicense?.let {
            ReplaceYearsForMaximumLicense(
                contractContentPartnerId = id,
                yearsForMaximumLicense = it
            )
        }
    }

    fun updateDaysForSellOffPeriod(): ContractContentPartnerUpdateCommand? {
        return updateContract.daysForSellOffPeriod?.let {
            ReplaceDaysForSellOffPeriod(
                contractContentPartnerId = id,
                daysForSellOffPeriod = it
            )
        }
    }

    fun updateRoyaltySplit(): ContractContentPartnerUpdateCommand? {
        return updateContract.royaltySplit?.let {
            ReplaceRoyaltySplit(
                contractContentPartnerId = id,
                royaltySplit = royaltySplitConverter.fromResource(it)
            )
        }
    }

    fun updateMinimumPriceDescription(): ContractContentPartnerUpdateCommand? {
        return updateContract.minimumPriceDescription?.let {
            ReplaceMinimumPriceDescription(
                contractContentPartnerId = id,
                minimumPriceDescription = it
            )
        }
    }

    fun updateRemittanceCurrency(): ContractContentPartnerUpdateCommand? {
        return updateContract.remittanceCurrency?.let {
            ReplaceRemittanceCurrency(
                contractContentPartnerId = id,
                remittanceCurrency = remittanceCurrencyConverter.fromResource(it)
            )
        }
    }

    fun updateRestrictions(): ContractContentPartnerUpdateCommand? {
        return updateContract.restrictions?.let {
            ReplaceRestrictions(
                contractContentPartnerId = id,
                restrictions = restrictionsConverter.fromResource(it)
            )
        }
    }

    fun updateCost(): ContractContentPartnerUpdateCommand? {
        return updateContract.costs?.let {
            ReplaceCost(
                contractContentPartnerId = id,
                costs = costsConverter.fromResource(it)
            )
        }
    }
}