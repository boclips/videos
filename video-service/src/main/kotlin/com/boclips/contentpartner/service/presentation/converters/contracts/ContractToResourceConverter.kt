package com.boclips.contentpartner.service.presentation.converters.contracts

import com.boclips.contentpartner.service.common.ResultsPage
import com.boclips.contentpartner.service.domain.model.contract.Contract
import com.boclips.contentpartner.service.presentation.hateoas.ContractsLinkBuilder
import com.boclips.videos.api.request.Projection
import com.boclips.videos.api.response.contract.ContractCostsResource
import com.boclips.videos.api.response.contract.ContractDatesResource
import com.boclips.videos.api.response.contract.ContractResource
import com.boclips.videos.api.response.contract.ContractRestrictionsResource
import com.boclips.videos.api.response.contract.ContractRoyaltySplitResource
import com.boclips.videos.api.response.contract.ContractsResource
import com.boclips.videos.api.response.contract.ContractsWrapperResource
import org.springframework.hateoas.PagedModel
import java.time.format.DateTimeFormatter

class ContractToResourceConverter(
    private val linksBuilderLegacy: ContractsLinkBuilder
) {
    fun convert(contract: Contract, projection: Projection? = Projection.full): ContractResource {
        return when (projection) {
            Projection.list -> buildContractListResource(contract)
            else -> {
                val formatter = DateTimeFormatter.ISO_DATE
                return ContractResource(
                    id = contract.id.value,
                    contentPartnerName = contract.contentPartnerName,
                    contractDocument = contract.contractDocument?.toString(),
                    contractDates = ContractDatesResource(
                        start = contract.contractDates?.start?.format(formatter),
                        end = contract.contractDates?.end?.format(formatter)
                    ),
                    contractIsRolling = contract.contractIsRolling,
                    daysBeforeTerminationWarning = contract.daysBeforeTerminationWarning,
                    yearsForMaximumLicense = contract.yearsForMaximumLicense,
                    daysForSellOffPeriod = contract.daysForSellOffPeriod,
                    royaltySplit = ContractRoyaltySplitResource(
                        download = contract.royaltySplit?.download,
                        streaming = contract.royaltySplit?.streaming
                    ),
                    minimumPriceDescription = contract.minimumPriceDescription,
                    remittanceCurrency = contract.remittanceCurrency?.currencyCode,
                    restrictions = ContractRestrictionsResource(
                        clientFacing = contract.restrictions?.clientFacing,
                        territory = contract.restrictions?.territory,
                        licensing = contract.restrictions?.licensing,
                        editing = contract.restrictions?.editing,
                        marketing = contract.restrictions?.marketing,
                        companies = contract.restrictions?.companies,
                        payout = contract.restrictions?.payout,
                        other = contract.restrictions?.other
                    ),
                    costs = ContractCostsResource(
                        minimumGuarantee = contract.costs.minimumGuarantee,
                        upfrontLicense = contract.costs.upfrontLicense,
                        technicalFee = contract.costs.technicalFee,
                        recoupable = contract.costs.recoupable
                    ),

                    _links = listOfNotNull(linksBuilderLegacy.self(contract.id.value)).map { it.rel to it }.toMap()
                )
            }
        }
    }

    fun convert(contracts: ResultsPage<Contract>, projection: Projection? = Projection.full): ContractsResource {
        return ContractsResource(
            _embedded = ContractsWrapperResource(
                contracts = contracts.elements.map { convert(it, projection) }
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

    private fun buildContractListResource(contract: Contract): ContractResource {
        return ContractResource(
            id = contract.id.value,
            contentPartnerName = contract.contentPartnerName,
            _links = listOfNotNull(linksBuilderLegacy.self(contract.id.value)).map { it.rel to it }.toMap()
        )
    }
}
