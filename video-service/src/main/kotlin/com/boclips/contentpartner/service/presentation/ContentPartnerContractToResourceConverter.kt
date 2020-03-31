package com.boclips.contentpartner.service.presentation

import com.boclips.contentpartner.service.common.ResultsPage
import com.boclips.contentpartner.service.domain.model.ContentPartnerContract
import com.boclips.contentpartner.service.presentation.hateoas.ContentPartnerContractsLinkBuilder
import com.boclips.videos.api.response.contract.ContentPartnerContractCostsResource
import com.boclips.videos.api.response.contract.ContentPartnerContractDatesResource
import com.boclips.videos.api.response.contract.ContentPartnerContractResource
import com.boclips.videos.api.response.contract.ContentPartnerContractRestrictionsResource
import com.boclips.videos.api.response.contract.ContentPartnerContractRoyaltySplitResource
import com.boclips.videos.api.response.contract.ContentPartnerContractsResource
import com.boclips.videos.api.response.contract.ContentPartnerContractsWrapperResource
import org.springframework.hateoas.PagedModel
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
            restrictions = ContentPartnerContractRestrictionsResource(
                clientFacing = contract.restrictions.clientFacing,
                territory = contract.restrictions.territory,
                licensing = contract.restrictions.licensing,
                editing = contract.restrictions.editing,
                marketing = contract.restrictions.marketing,
                companies = contract.restrictions.companies,
                payout = contract.restrictions.payout,
                other = contract.restrictions.other
            ),
            costs = ContentPartnerContractCostsResource(
                minimumGuarantee = contract.costs.minimumGuarantee,
                upfrontLicense = contract.costs.upfrontLicense,
                technicalFee = contract.costs.technicalFee,
                recoupable = contract.costs.recoupable
            ),

            _links = listOfNotNull(linksBuilder.self(contract.id.value)).map { it.rel to it }.toMap()
        )
    }

    fun convert(contracts: ResultsPage<ContentPartnerContract>): ContentPartnerContractsResource {
        return ContentPartnerContractsResource(
            _embedded = ContentPartnerContractsWrapperResource(
                contracts = contracts.elements.map { convert(it) }
            ),
            _links = null,
            page = PagedModel.PageMetadata(
                contracts.pageInfo.pageRequest.size.toLong(),
                contracts.pageInfo.pageRequest.page.toLong(),
                contracts.pageInfo.totalElements,
                contracts.pageInfo.totalPages
            )
        )
    }
}
