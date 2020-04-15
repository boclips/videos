package com.boclips.contentpartner.service.presentation.converters.contracts

import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContractDates
import com.boclips.videos.api.response.contract.ContentPartnerContractDatesResource
import java.time.LocalDate

class ContractDatesToResourceConverter {
    fun fromResource(contractDates: ContentPartnerContractDatesResource): ContractDates {
        return contractDates.let {
            ContractDates(start = LocalDate.parse(it.start), end = LocalDate.parse(it.end))
        }
    }
}