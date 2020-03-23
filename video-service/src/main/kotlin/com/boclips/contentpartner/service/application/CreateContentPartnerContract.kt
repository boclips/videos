package com.boclips.contentpartner.service.application

import com.boclips.contentpartner.service.domain.model.ContentPartnerContract
import com.boclips.contentpartner.service.domain.model.ContentPartnerContractDates
import com.boclips.contentpartner.service.domain.model.ContentPartnerContractId
import com.boclips.contentpartner.service.domain.model.ContentPartnerContractRepository
import com.boclips.contentpartner.service.domain.model.ContentPartnerContractRoyaltySplit
import com.boclips.contentpartner.service.presentation.CurrencyConverter
import com.boclips.contentpartner.service.presentation.DateConverter
import com.boclips.contentpartner.service.presentation.UrlConverter
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
                contractDates = ContentPartnerContractDates(
                    start = request.contractDates?.start?.let(DateConverter::convert),
                    end = request.contractDates?.end?.let(DateConverter::convert)
                ),
                daysBeforeTerminationWarning = request.daysBeforeTerminationWarning,
                yearsForMaximumLicense = request.yearsForMaximumLicense,
                daysForSellOffPeriod = request.daysForSellOffPeriod,
                royaltySplit = ContentPartnerContractRoyaltySplit(
                    download = request.royaltySplit?.download,
                    streaming = request.royaltySplit?.streaming
                ),
                minimumPriceDescription = request.minimumPriceDescription,
                remittanceCurrency = request.remittanceCurrency?.let(CurrencyConverter::convert)
            )
        )
}