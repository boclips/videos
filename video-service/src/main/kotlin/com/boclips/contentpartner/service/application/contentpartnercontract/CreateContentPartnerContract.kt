package com.boclips.contentpartner.service.application.contentpartnercontract

import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContract
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContractCosts
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContractDates
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractId
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractRepository
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContractRestrictions
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContractRoyaltySplit
import com.boclips.contentpartner.service.presentation.converters.CurrencyConverter
import com.boclips.contentpartner.service.presentation.converters.DateConverter
import com.boclips.contentpartner.service.presentation.converters.UrlConverter
import com.boclips.videos.api.request.contract.ContentPartnerContractRequest
import org.bson.types.ObjectId

class CreateContentPartnerContract(
    private val contentPartnerContractRepository: ContentPartnerContractRepository
) {
    operator fun invoke(request: ContentPartnerContractRequest): ContentPartnerContractId =
        contentPartnerContractRepository.create(
            ContentPartnerContract(
                id = ObjectId().toHexString().let(::ContentPartnerContractId),
                contentPartnerName = request.contentPartnerName,
                contractDocument = request.contractDocument?.let(UrlConverter::convert),
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
}
