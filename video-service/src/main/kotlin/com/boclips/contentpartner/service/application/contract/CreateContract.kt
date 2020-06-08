package com.boclips.contentpartner.service.application.contract

import com.boclips.contentpartner.service.application.exceptions.ContractConflictException
import com.boclips.contentpartner.service.domain.model.contract.CreateContractResult
import com.boclips.contentpartner.service.domain.model.contract.Contract
import com.boclips.contentpartner.service.domain.model.contract.ContractId
import com.boclips.contentpartner.service.domain.model.contract.ContractCosts
import com.boclips.contentpartner.service.domain.model.contract.ContractDates
import com.boclips.contentpartner.service.domain.model.contract.ContractRestrictions
import com.boclips.contentpartner.service.domain.model.contract.ContractRoyaltySplit
import com.boclips.contentpartner.service.domain.service.contract.ContractService
import com.boclips.contentpartner.service.presentation.converters.CurrencyConverter
import com.boclips.contentpartner.service.presentation.converters.DateConverter
import com.boclips.contentpartner.service.presentation.converters.UrlConverter
import com.boclips.videos.api.common.ExplicitlyNull
import com.boclips.videos.api.common.Specified
import com.boclips.videos.api.request.contract.CreateContractRequest
import org.bson.types.ObjectId

class CreateContract(
    private val contractService: ContractService
) {
    operator fun invoke(request: CreateContractRequest): Contract {
        val result = contractService.create(
            Contract(
                id = ObjectId().toHexString().let(::ContractId),
                contentPartnerName = request.contentPartnerName,
                contractDocument = when (request.contractDocument) {
                    // https://discuss.kotlinlang.org/t/what-is-the-reason-behind-smart-cast-being-impossible-to-perform-when-referenced-class-is-in-another-module/2201/8
                    is Specified -> UrlConverter.convert((request.contractDocument as Specified<String>).value)
                    is ExplicitlyNull -> null
                    null -> null
                },
                contractDates = ContractDates(
                    start = request.contractDates?.start?.let(DateConverter::convert),
                    end = request.contractDates?.end?.let(DateConverter::convert)
                ),
                contractIsRolling = request.contractIsRolling,
                daysBeforeTerminationWarning = request.daysBeforeTerminationWarning,
                yearsForMaximumLicense = request.yearsForMaximumLicense,
                daysForSellOffPeriod = request.daysForSellOffPeriod,
                royaltySplit = ContractRoyaltySplit(
                    download = request.royaltySplit?.download,
                    streaming = request.royaltySplit?.streaming
                ),
                minimumPriceDescription = request.minimumPriceDescription,
                remittanceCurrency = request.remittanceCurrency?.let(CurrencyConverter::convert),
                restrictions = ContractRestrictions(
                    clientFacing = request.restrictions?.clientFacing ?: emptyList(),
                    territory = request.restrictions?.territory,
                    licensing = request.restrictions?.licensing,
                    editing = request.restrictions?.editing,
                    marketing = request.restrictions?.marketing,
                    companies = request.restrictions?.companies,
                    payout = request.restrictions?.payout,
                    other = request.restrictions?.other
                ),
                costs = ContractCosts(
                    minimumGuarantee = request.costs?.minimumGuarantee ?: emptyList(),
                    upfrontLicense = request.costs?.upfrontLicense,
                    technicalFee = request.costs?.technicalFee,
                    recoupable = request.costs?.recoupable ?: false
                )
            )
        )

        return when (result) {
            is CreateContractResult.Success -> result.contract
            is CreateContractResult.NameConflict ->
                throw ContractConflictException(request.contentPartnerName)
        }
    }
}
