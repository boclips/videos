package com.boclips.contentpartner.service.presentation

import com.boclips.contentpartner.service.domain.model.ContentPartnerContract
import com.boclips.contentpartner.service.presentation.hateoas.ContentPartnerContractsLinkBuilder
import com.boclips.videos.api.response.contract.ContentPartnerContractDatesResource
import com.boclips.videos.api.response.contract.ContentPartnerContractResource
import com.boclips.videos.api.response.contract.ContentPartnerContractRoyaltySplitResource
import java.time.format.DateTimeFormatter

class ContentPartnerContractToResourceConverter(
    private val linksBuilder: ContentPartnerContractsLinkBuilder
) {
    fun convert(contract: ContentPartnerContract): ContentPartnerContractResource {
        val formatter = DateTimeFormatter.ISO_DATE
        return ContentPartnerContractResource(
            id = contract.id.value,
            contentPartnerName = contract.contentPartnerName,
            contractDocument = contract.contractDocument.toString(),
            contractDates = ContentPartnerContractDatesResource(
                start = contract.contractDates?.start?.format(formatter),
                end = contract.contractDates?.end?.format(formatter)
            ),
            daysBeforeTerminationWarning = contract.daysBeforeTerminationWarning,
            yearsForMaximumLicense = contract.yearsForMaximumLicense,
            daysForSellOffPeriod = contract.daysForSellOffPeriod,
            royaltySplit = ContentPartnerContractRoyaltySplitResource(
                download = contract.royaltySplit?.download,
                streaming = contract.royaltySplit?.streaming
            ),
            minimumPriceDescription = contract.minimumPriceDescription,
            remittanceCurrency = contract.remittanceCurrency?.currencyCode,
            _links = listOf(linksBuilder.self(contract.id.value)).map { it.rel to it }.toMap()
        )
    }
}