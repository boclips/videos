package com.boclips.contentpartner.service.presentation.converters.contracts

import com.boclips.contentpartner.service.domain.model.contract.ContractDates
import com.boclips.videos.api.response.contract.ContentPartnerContractDatesResource
import java.time.LocalDate

class ContractDatesToResourceConverter {
    fun fromResource(contractDates: ContentPartnerContractDatesResource?): ContractDates? {
        return contractDates?.let {
            ContractDates(
                start = it.start?.let { start ->
                    LocalDate.parse(start)
                },
                end = it.end?.let { end ->
                    LocalDate.parse(end)
                }
            )
        }
    }
}
