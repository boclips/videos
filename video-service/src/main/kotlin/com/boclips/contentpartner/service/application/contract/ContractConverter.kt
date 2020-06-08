package com.boclips.contentpartner.service.application.contract

import com.boclips.contentpartner.service.domain.model.contract.ContractId
import com.boclips.contentpartner.service.domain.model.contract.ContractUpdateCommand
import com.boclips.contentpartner.service.domain.model.contract.ContractUpdateCommand.ReplaceContentPartnerName
import com.boclips.contentpartner.service.domain.model.contract.ContractUpdateCommand.ReplaceContractDates
import com.boclips.contentpartner.service.domain.model.contract.ContractUpdateCommand.ReplaceContractDocument
import com.boclips.contentpartner.service.domain.model.contract.ContractUpdateCommand.ReplaceContractIsRolling
import com.boclips.contentpartner.service.domain.model.contract.ContractUpdateCommand.ReplaceCost
import com.boclips.contentpartner.service.domain.model.contract.ContractUpdateCommand.ReplaceDaysBeforeTerminationWarning
import com.boclips.contentpartner.service.domain.model.contract.ContractUpdateCommand.ReplaceDaysForSellOffPeriod
import com.boclips.contentpartner.service.domain.model.contract.ContractUpdateCommand.ReplaceMinimumPriceDescription
import com.boclips.contentpartner.service.domain.model.contract.ContractUpdateCommand.ReplaceRemittanceCurrency
import com.boclips.contentpartner.service.domain.model.contract.ContractUpdateCommand.ReplaceRestrictions
import com.boclips.contentpartner.service.domain.model.contract.ContractUpdateCommand.ReplaceRoyaltySplit
import com.boclips.contentpartner.service.domain.model.contract.ContractUpdateCommand.ReplaceYearsForMaximumLicense
import com.boclips.contentpartner.service.presentation.converters.contracts.ContractCostsConverter
import com.boclips.contentpartner.service.presentation.converters.contracts.ContractDatesToResourceConverter
import com.boclips.contentpartner.service.presentation.converters.contracts.ContractRemittanceCurrencyConverter
import com.boclips.contentpartner.service.presentation.converters.contracts.ContractRestrictionsConverter
import com.boclips.contentpartner.service.presentation.converters.contracts.ContractRoyaltySplitConverter
import com.boclips.videos.api.common.ExplicitlyNull
import com.boclips.videos.api.common.Specified
import com.boclips.videos.api.request.contract.UpdateContractRequest

class ContractConverter(
    private val contractDatesConverter: ContractDatesToResourceConverter,
    private val royaltySplitConverter: ContractRoyaltySplitConverter,
    private val remittanceCurrencyConverter: ContractRemittanceCurrencyConverter,
    private val restrictionsConverter: ContractRestrictionsConverter,
    private val costsConverter: ContractCostsConverter
) {
    fun convert(
        id: ContractId,
        updateContract: UpdateContractRequest
    ): List<ContractUpdateCommand> =
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
    val id: ContractId,
    private val updateContract: UpdateContractRequest,
    private val contractDatesConverter: ContractDatesToResourceConverter,
    private val royaltySplitConverter: ContractRoyaltySplitConverter,
    private val remittanceCurrencyConverter: ContractRemittanceCurrencyConverter,
    private val restrictionsConverter: ContractRestrictionsConverter,
    private val costsConverter: ContractCostsConverter
) {
    fun updateContractContentPartnerName(): ContractUpdateCommand? {
        return updateContract.contentPartnerName?.let {
            ReplaceContentPartnerName(
                contractId = id,
                contentPartnerName = it
            )
        }
    }

    fun updateContractDocument(): ContractUpdateCommand? {
        return updateContract.contractDocument?.let {
            ReplaceContractDocument(
                contractId = id,
                contractDocument = when (it) {
                    is Specified -> it.value
                    is ExplicitlyNull -> null
                }
            )
        }
    }

    fun updateContractIsRolling(): ContractUpdateCommand? {
        return updateContract.contractIsRolling?.let {
            ReplaceContractIsRolling(
                contractId = id,
                contractIsRolling = it
            )
        }
    }

    fun updateContractDates(): ContractUpdateCommand? {
        return updateContract.contractDates?.let {
            contractDatesConverter.fromResource(it)?.let { dates ->
                ReplaceContractDates(
                    contractId = id,
                    contractDates = dates
                )
            }
        }
    }

    fun updateDaysBeforeTerminationWarning(): ContractUpdateCommand? {
        return updateContract.daysBeforeTerminationWarning?.let {
            ReplaceDaysBeforeTerminationWarning(
                contractId = id,
                daysBeforeTerminationWarning = it
            )
        }
    }

    fun updateYearsForMaximumLicense(): ContractUpdateCommand? {
        return updateContract.yearsForMaximumLicense?.let {
            ReplaceYearsForMaximumLicense(
                contractId = id,
                yearsForMaximumLicense = it
            )
        }
    }

    fun updateDaysForSellOffPeriod(): ContractUpdateCommand? {
        return updateContract.daysForSellOffPeriod?.let {
            ReplaceDaysForSellOffPeriod(
                contractId = id,
                daysForSellOffPeriod = it
            )
        }
    }

    fun updateRoyaltySplit(): ContractUpdateCommand? {
        return updateContract.royaltySplit?.let {
            ReplaceRoyaltySplit(
                contractId = id,
                royaltySplit = royaltySplitConverter.fromResource(it)
            )
        }
    }

    fun updateMinimumPriceDescription(): ContractUpdateCommand? {
        return updateContract.minimumPriceDescription?.let {
            ReplaceMinimumPriceDescription(
                contractId = id,
                minimumPriceDescription = it
            )
        }
    }

    fun updateRemittanceCurrency(): ContractUpdateCommand? {
        return updateContract.remittanceCurrency?.let {
            ReplaceRemittanceCurrency(
                contractId = id,
                remittanceCurrency = remittanceCurrencyConverter.fromResource(it)
            )
        }
    }

    fun updateRestrictions(): ContractUpdateCommand? {
        return updateContract.restrictions?.let {
            ReplaceRestrictions(
                contractId = id,
                restrictions = restrictionsConverter.fromResource(it)
            )
        }
    }

    fun updateCost(): ContractUpdateCommand? {
        return updateContract.costs?.let {
            ReplaceCost(
                contractId = id,
                costs = costsConverter.fromResource(it)
            )
        }
    }
}
